import {UserRole} from "./user-role";

export interface UserDTO {
  uuid: string
  username: string
  firstName: string
  lastName: string
  email: string
  role: UserRole
  password: string
}

export class User implements UserDTO {
  email: string;
  firstName: string;
  lastName: string;
  password: string;
  role: UserRole;
  username: string;
  uuid: string;

  static from(from: UserDTO): User {
    let user = new User();
    user.uuid = from.uuid;
    user.username = from.username;
    user.firstName = from.firstName;
    user.lastName = from.lastName;
    user.email = from.email;
    user.role = from.role;
    user.password = from.password;
    return user;
  }

  public hasRolePermission(role: UserRole): boolean {
    if (this.role === UserRole.ADMIN) {
      return true;
    } else if (this.role === UserRole.DOER) {
      return role === UserRole.VIEWER || role === UserRole.DOER;
    } else {
      return this.role === role;
    }
  }

  public hasAnyPermission(...roles: Array<UserRole>): boolean {
    for (let role of roles) {
      if (this.hasRolePermission(role)) {
        return true;
      }
    }
    return false;
  }

  public displayName(): string {
    return (this.firstName && this.lastName) ? `${this.firstName} ${this.lastName}` : this.username;
  }
}
