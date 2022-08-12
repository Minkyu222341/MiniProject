package sparta.miniproject.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import sparta.miniproject.model.Authority;
import sparta.miniproject.model.Member;

@Getter
@AllArgsConstructor
@NoArgsConstructor
public class MemberRequestDto {
//
   private String nickname;

   private String password;

   private String passwordConfirm;

   public Member toMember(PasswordEncoder passwordEncoder) {
      return Member.builder()
              .nickname(nickname)
              .password(passwordEncoder.encode(password))
              .authority(Authority.ROLE_USER)
              .build();
   }

   public UsernamePasswordAuthenticationToken toAuthentication() {
      return new UsernamePasswordAuthenticationToken(nickname, password);
   }
}
