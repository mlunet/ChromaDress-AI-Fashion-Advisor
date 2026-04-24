export interface AuthResponseDTO {
  readonly accessToken: string;
  readonly refreshToken: string;
  readonly message: string;
}

export interface LoginDTO {
  readonly username: string;
  readonly password: string;
}

export interface RegistrationDTO {
  readonly username: string;
  readonly password: string;
  readonly email: string;
}

export interface RefreshTokenDTO {
  readonly refreshToken: string;
}
