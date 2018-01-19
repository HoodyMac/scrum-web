package scrumweb.common.asm;

import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Component;
import scrumweb.dto.UserDto;
import scrumweb.user.account.domain.UserAccount;
import scrumweb.user.profile.domain.UserProfile;

@Component
public class UserAccountAsm {

    public UserAccount makeUserAccount(UserDto userDto, UserProfile userProfile) {
        return new UserAccount(userDto.getUsername(), new BCryptPasswordEncoder().encode(userDto.getPassword()), userProfile);
    }
}