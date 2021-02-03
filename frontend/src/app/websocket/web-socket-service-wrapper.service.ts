import {Injectable} from "@angular/core";
import {Observable} from "rxjs/internal/Observable";
import {filter, map} from "rxjs/operators";
import {Activity} from "../activity/activity";
import {LogService} from "../util/log.service";
import {
  ActivityMessage,
  DeploymentStatusChangedMessage,
  ONekoWebsocketMessage,
  WebsocketMessageMapping,
  WebsocketMessageType
} from "./message/websocket-message";
import {WebSocketService} from "./web-socket.service";

@Injectable()
export class WebSocketServiceWrapper {

  private log = LogService.getLogger(WebSocketServiceWrapper);

  constructor(private websocket: WebSocketService) {
  }

  public getProjectVersionChanges(projectId: string): Observable<DeploymentStatusChangedMessage> {
    return this.streamType(DeploymentStatusChangedMessage)
      .pipe(
        filter(msg => msg.ownerId === projectId)
      );
  }

  public getActivityStream(): Observable<Activity> {
    return this.streamType(ActivityMessage)
      .pipe(map(message => message.activity));
  }

  private streamType<T extends ONekoWebsocketMessage>(type: WebsocketMessageType<T>): Observable<T> {
    return this.stream().pipe(
      filter(message => message instanceof type),
      map(message => message as T)
    );
  }

  private stream(): Observable<ONekoWebsocketMessage> {
    return this.websocket.stream().pipe(
      filter((message: any) => message.type !== undefined),
      map((message: any) => WebsocketMessageMapping[message.type] ? new WebsocketMessageMapping[message.type](message) : message.type),
      filter(messageOrType => {
        if (typeof messageOrType === 'string') {
          this.log.warn(`Received message with unknown message type: ${messageOrType}`);
          return false;
        }
        return true;
      })
    );
  }

}
