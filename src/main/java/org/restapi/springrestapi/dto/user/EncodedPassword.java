package org.restapi.springrestapi.dto.user;

import org.restapi.springrestapi.common.annotation.ValidPassword;

public record EncodedPassword(
        @ValidPassword String value
) { }
