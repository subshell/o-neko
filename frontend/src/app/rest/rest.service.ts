import {HttpClient, HttpHeaders} from "@angular/common/http";
import {Injectable} from "@angular/core";
import {Observable} from "rxjs";
import {map, tap} from "rxjs/operators";
import {AuthService} from "../session/auth.service";
import {ChangePasswordDTO} from "../user/change-password-dto";
import {User, UserDTO} from "../user/user";
import {ActivityRestService} from "./activity-rest.service";
import {DefinedNamespaceRestService} from "./defined-namespace-rest.service";
import {DockerRestService} from "./docker-rest.service";
import {ProjectRestService} from "./project-rest.service";
import {HelmRestService} from "./helm-rest.service";
import {LogsRestService} from "./logs-rest.service";

@Injectable()
export class RestService {
  private static readonly ROOT_PATH = 'api';
  private readonly dockerRestService: DockerRestService;
  private readonly helmRestService: HelmRestService;
  private readonly projectRestService: ProjectRestService;
  private readonly activityRestService: ActivityRestService;
  private readonly namespaceRestService: DefinedNamespaceRestService;
  private readonly logsRestService: LogsRestService;

  constructor(private http: HttpClient, private authService: AuthService) {
    this.dockerRestService = new DockerRestService(this.http, RestService.ROOT_PATH);
    this.helmRestService = new HelmRestService(this.http, RestService.ROOT_PATH);
    this.projectRestService = new ProjectRestService(this.http, RestService.ROOT_PATH);
    this.activityRestService = new ActivityRestService(this.http, RestService.ROOT_PATH);
    this.namespaceRestService = new DefinedNamespaceRestService(this.http, RestService.ROOT_PATH);
    this.logsRestService = new LogsRestService(this.http, RestService.ROOT_PATH);
  }

  /*------------------------------------------------------------
   * Session and User stuff
   ------------------------------------------------------------*/

  public login(username: string, password: string): Observable<any> {
    const params = new URLSearchParams({
      username,
      password
    });

    const options = {
      headers: new HttpHeaders().set('Content-Type', 'application/x-www-form-urlencoded')
    };

    return this.http.post('/api/session/login', params.toString(), options).pipe(tap(ev => {
      this.authService.setAuthenticated(true);
    }));
  }

  public logout(): Observable<any> {
    return this.http.post('/api/session/logout', {}).pipe(tap(ev => {
      this.authService.setAuthenticated(false);
    }));
  }

  public isLoggedIn(): Observable<User> {
    return this.currentUser();
  }

  public currentUser(): Observable<User> {
    return this.http.get<UserDTO>('/api/session').pipe(map(User.from));
  }

  public allUsers(): Observable<Array<User>> {
    return this.http.get<Array<UserDTO>>('/api/user')
      .pipe(map(users => users.map(User.from)));
  }

  public changePassword(username: string, passwordDto: ChangePasswordDTO): Observable<User> {
    return this.http.post<UserDTO>(`/api/user/${username}/password`, passwordDto).pipe(map(User.from));
  }

  /**
   *
   * @param {User} user The user object to update.
   * @param {string} currentUsername Used to update the username. This needs to be the current username, the new username should be inside the user object. If you're not updating the username, this parameter can be omitted.
   * @returns {Observable<User>}
   */
  public updateUser(user: User, currentUsername?: string): Observable<User> {
    return this.http.post<UserDTO>(`/api/user/${currentUsername ? currentUsername : user.username}`, user).pipe(map(User.from));
  }

  public createUser(user: User): Observable<User> {
    return this.http.post<UserDTO>('/api/user', user).pipe(map(User.from));
  }

  public deleteUser(user: User): Observable<any> {
    return this.http.delete(`/api/user/${user.username}`);
  }

  public isUsernameAvailable(username: string): Observable<boolean> {
    return this.http.get<any>(`/api/available/username/${username}`)
      .pipe(map(dto => dto.available));
  }

  public isEmailAvailable(email: string): Observable<boolean> {
    return this.http.get<any>(`/api/available/email/${email}`)
      .pipe(map(dto => dto.available));
  }


  /*------------------------------------------------------------
   * delegates
   ------------------------------------------------------------*/

  public project(): ProjectRestService {
    return this.projectRestService;
  }

  public docker(): DockerRestService {
    return this.dockerRestService;
  }

  public helm(): HelmRestService {
    return this.helmRestService;
  }

  public activity(): ActivityRestService {
    return this.activityRestService;
  }

  public namespace(): DefinedNamespaceRestService {
    return this.namespaceRestService;
  }

  public logs(): LogsRestService {
    return this.logsRestService;
  }
}
