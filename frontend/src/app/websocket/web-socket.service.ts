import {Injectable} from '@angular/core';
import {Observable, Subject} from 'rxjs';
import {LogService} from "../util/log.service";
import {ONekoWebsocketMessage} from "./message/websocket-message";

export type WebSocketState = number;

@Injectable()
export class WebSocketService {
  private _stream$: Subject<MessageEvent> = new Subject();
  private _ws: WebSocket;
  private _retryConnectionId: number = -1;

  private log = LogService.getLogger(WebSocketService);

  constructor() {
  }

  public get state(): WebSocketState | null {
    return this._ws ? this._ws.readyState : null;
  }

  public stream(): Observable<any> {
    return this._stream$.asObservable();
  }

  public send(msg: ONekoWebsocketMessage): void {
    if (this._ws.readyState === WebSocket.OPEN) {
      let payload = JSON.stringify(msg);
      this._ws.send(payload);
    }
  }

  public close() {
    if (!this._ws) return;

    this.log.info('Closing Websocket');

    if (this._ws.readyState === WebSocket.CLOSED) {
      this.log.debug('WebSocket is closed. Do not reconnect');
      clearTimeout(this._retryConnectionId);
    }

    this._ws.close(1000);
    this._ws = null;
  }

  public connect() {
    // only connect once
    if (this.state !== null) {
      return this;
    }

    this.log.debug(`Connecting to WebSocket url ${this.getConnectionUrl()}`);

    const reconnection = (event: CloseEvent) => {
      if (event.code === 1000) {
        return;
      }

      // reconnection
      this._retryConnectionId = window.setTimeout(() => {
        this.log.debug('Try reconnecting to WebSocket');
        try {
          this._ws = this.createWebSocket((event) => reconnection(event));
        } catch (e) {
          this.log.error('Could not reconnect to WebSocket. Will try again.', e);
        }
      }, 2000);
    };

    this._ws = this.createWebSocket(event => reconnection(event));
  }

  private getConnectionUrl(): string {
    const protocol = location.protocol === 'http:' ? 'ws:' : 'wss:';
    return `${protocol}//${location.host}/ws`;
  }

  private createWebSocket(onClose: (event: CloseEvent) => any): WebSocket {
    const ws = new WebSocket(this.getConnectionUrl());
    ws.onerror = (err) => {
      this.log.error("WebSocket error", err);
    };

    ws.onclose = event => {
      this.log.info('WebSocket connection closed');
      onClose(event);
    };

    ws.onopen = () => {
      this.log.info('WebSocket connection opened');
    };

    ws.onmessage = msg => {
      this.log.trace(`Received WebSocket message ${msg.data}`);
      this._stream$.next(JSON.parse(msg.data));
    };

    return ws;
  }
}
