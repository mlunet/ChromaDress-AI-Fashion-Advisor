package com.app.ChromaDress.auth;

import jakarta.validation.constraints.NotNull;

public record RegistrationDTO(@NotNull String username, @NotNull String password,
                              @NotNull String email) {

}
