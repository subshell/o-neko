export interface ApiTokenDTO {
  id: string;
  name: string;
  createdAt: string;
  expiresAt: string | null;
  lastUsedAt: string | null;
}

export interface CreateApiTokenRequest {
  name: string;
  expiresAt: string | null;
}

export interface CreateApiTokenResponse {
  token: ApiTokenDTO;
  rawToken: string;
}

export class ApiToken implements ApiTokenDTO {
  id: string;
  name: string;
  createdAt: string;
  expiresAt: string | null;
  lastUsedAt: string | null;

  static from(dto: ApiTokenDTO): ApiToken {
    const token = new ApiToken();
    token.id = dto.id;
    token.name = dto.name;
    token.createdAt = dto.createdAt;
    token.expiresAt = dto.expiresAt;
    token.lastUsedAt = dto.lastUsedAt;
    return token;
  }

  isExpired(): boolean {
    if (!this.expiresAt) {
      return false;
    }
    return new Date(this.expiresAt) < new Date();
  }
}
