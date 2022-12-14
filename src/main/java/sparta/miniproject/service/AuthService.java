package sparta.miniproject.service;

import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import sparta.miniproject.dto.MemberRequestDto;
import sparta.miniproject.dto.MemberResponseDto;
import sparta.miniproject.dto.TokenDto;
import sparta.miniproject.dto.TokenRequestDto;
import sparta.miniproject.jwt.TokenProvider;
import sparta.miniproject.model.Member;
import sparta.miniproject.model.RefreshToken;
import sparta.miniproject.repository.MemberRepository;
import sparta.miniproject.repository.RefreshTokenRepository;

import java.util.Optional;
import java.util.regex.Pattern;

@Service
@RequiredArgsConstructor
public class AuthService {
    private final AuthenticationManagerBuilder authenticationManagerBuilder;
    private final MemberRepository memberRepository;
    private final PasswordEncoder passwordEncoder;
    private final TokenProvider tokenProvider;
    private final RefreshTokenRepository refreshTokenRepository;

    public String getLoginMemberNickname() {
        String userId = SecurityContextHolder.getContext().getAuthentication().getName();
        Optional<Member> member = memberRepository.findById(Long.valueOf(userId));
        return member.get().getNickname();
    }

    @Transactional
    public MemberResponseDto signup(MemberRequestDto memberRequestDto) {
        if (!(Pattern.matches("^[a-zA-Z](?=.{0,28}[0-9])[0-9a-zA-Z]{4,15}$", memberRequestDto.getUsername()) && (memberRequestDto.getUsername().length() > 3 && memberRequestDto.getUsername().length() < 13)
                && Pattern.matches("^(?=.*[a-zA-Z])(?=.*[!@#$%^*+=-])(?=.*[0-9]).{8,25}$", memberRequestDto.getPassword()) && (memberRequestDto.getPassword().length() > 3 && memberRequestDto.getPassword().length() < 33))) {
            throw new IllegalArgumentException("????????? ?????? ???????????? ????????? ??????????????????.");
        }

        Member member = memberRequestDto.toMember(passwordEncoder);
        return MemberResponseDto.of(memberRepository.save(member));
    }

    @Transactional
    public TokenDto login(MemberRequestDto memberRequestDto) {
        // 1. Login ID/PW ??? ???????????? AuthenticationToken ??????
        UsernamePasswordAuthenticationToken authenticationToken = memberRequestDto.toAuthentication();

        // 2. ????????? ?????? (????????? ???????????? ??????) ??? ??????????????? ??????
        //    authenticate ???????????? ????????? ??? ??? CustomUserDetailsService ?????? ???????????? loadUserByUsername ???????????? ?????????
        try {
            Authentication authentication = authenticationManagerBuilder.getObject().authenticate(authenticationToken);

            // 3. ?????? ????????? ???????????? JWT ?????? ??????
            TokenDto tokenDto = tokenProvider.generateTokenDto(authentication);

            // 4. RefreshToken ??????
            RefreshToken refreshToken = RefreshToken.builder()
                    .key(authentication.getName())
                    .value(tokenDto.getRefreshToken())
                    .build();

//            refreshTokenRepository.save(refreshToken);

            // 5. ?????? ??????
            return tokenDto;
        } catch (Exception e) {
            throw new IllegalArgumentException("???????????? ?????? ??? ????????????.");
        }
    }


    public boolean validateUsername(MemberRequestDto memberRequestDto) {
        boolean flag = true;
        if (memberRepository.existsByUsername(memberRequestDto.getUsername())) {
            flag = false;
        }
        return flag;
    }

    public boolean validateNickname(MemberRequestDto memberRequestDto) {
        boolean flag = true;
        if (memberRepository.existsByNickname(memberRequestDto.getNickname())) {
            flag = false;
        }
        return flag;
    }

    public String getLoginNickname() {
        return getLoginMemberNickname();
    }


    @Transactional
    public TokenDto reissue(TokenRequestDto tokenRequestDto) {
        // 1. Refresh Token ??????
        if (!tokenProvider.validateToken(tokenRequestDto.getRefreshToken())) {
            throw new RuntimeException("Refresh Token ??? ???????????? ????????????.");
        }

        // 2. Access Token ?????? Member ID ????????????
        Authentication authentication = tokenProvider.getAuthentication(tokenRequestDto.getAccessToken());

        // 3. ??????????????? Member ID ??? ???????????? Refresh Token ??? ?????????
        RefreshToken refreshToken = refreshTokenRepository.findByKey(authentication.getName())
                .orElseThrow(() -> new RuntimeException("???????????? ??? ??????????????????."));

        // 4. Refresh Token ??????????????? ??????
        if (!refreshToken.getValue().equals(tokenRequestDto.getRefreshToken())) {
            throw new RuntimeException("????????? ?????? ????????? ???????????? ????????????.");
        }

        // 5. ????????? ?????? ??????
        TokenDto tokenDto = tokenProvider.generateTokenDto(authentication);

        // 6. ????????? ?????? ????????????
        RefreshToken newRefreshToken = refreshToken.updateValue(tokenDto.getRefreshToken());
        refreshTokenRepository.save(newRefreshToken);

        // ?????? ??????
        return tokenDto;
    }
}