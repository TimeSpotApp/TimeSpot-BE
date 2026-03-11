package com.timespot.backend.domain.user.constant;

import java.util.regex.Pattern;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;

/**
 * PackageName : com.timespot.backend.domain.user.constant
 * FileName    : UserConst
 * Author      : loadingKKamo21
 * Date        : 26. 3. 8.
 * Description :
 * =====================================================================================================================
 * DATE          AUTHOR               DESCRIPTION
 * ---------------------------------------------------------------------------------------------------------------------
 * 26. 3. 8.     loadingKKamo21       Initial creation
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public abstract class UserConst {

    public static final String EMAIL_REGEX    = "^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,}$";
    public static final String NICKNAME_REGEX = "^[a-zA-Z0-9가-힣_-]{2,15}$";

    public static final Pattern EMAIL_PATTERN    = Pattern.compile(EMAIL_REGEX);
    public static final Pattern NICKNAME_PATTERN = Pattern.compile(NICKNAME_REGEX);

}
