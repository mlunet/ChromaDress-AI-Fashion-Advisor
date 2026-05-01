package com.app.ChromaDress.auth;

import jakarta.validation.constraints.NotNull;

public record LoginDTO(@NotNull String username, @NotNull String password) {

}
