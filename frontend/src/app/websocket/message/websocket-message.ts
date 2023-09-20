import {Activity} from "../../activity/activity";
import {DeployableStatus, DesiredState} from "../../deployable/deployment";

export interface WebsocketMessageType<T extends ONekoWebsocketMessage> {
  new(...args: any[]): T;
}

export interface ONekoWebsocketMessage {
}

export class TestMessage implements ONekoWebsocketMessage {
  public test: string;

  constructor(rawMessage: any) {
    this.test = rawMessage.test;
  }
}

export class DeploymentStatusChangedMessage implements ONekoWebsocketMessage {
  public projectVersionId: string;
  public ownerId: string;
  public status: DeployableStatus;
  public desiredState: DesiredState;
  public timestamp: Date;
  public outdated: boolean;
  public imageUpdatedDate: Date;

  constructor(rawMessage: any) {
    this.projectVersionId = rawMessage.projectVersionId;
    this.ownerId = rawMessage.ownerId;
    this.status = rawMessage.status;
    this.desiredState = rawMessage.desiredState;
    this.timestamp = rawMessage.timestamp ? new Date(rawMessage.timestamp) : null;
    this.outdated = rawMessage.outdated;
    this.imageUpdatedDate = rawMessage.imageUpdatedDate ? new Date(rawMessage.imageUpdatedDate) : null;
  }
}

export class ActivityMessage implements ONekoWebsocketMessage {
  public activity: Activity;

  constructor(rawMessage: any) {
    this.activity = new Activity(rawMessage.activity);
  }
}

export class SubscribeToLogsMessage implements ONekoWebsocketMessage {
  public readonly  type = 'SubscribeToLogsMessage';
  public projectId: string;
  public versionId: string;
  public pod: string;
  public container?: string;
  constructor(projectId: string, versionId: string, pod: string, container?: string) {
    this.projectId = projectId;
    this.versionId = versionId;
    this.pod = pod;
    this.container = container;
  }
}

export class UnsubscribeFromLogsMessage implements ONekoWebsocketMessage {
  public readonly  type = 'UnsubscribeFromLogsMessage';
}

export class LogsMessage implements ONekoWebsocketMessage {
  public timestamp: Date;
  public lines: Array<string>;

  constructor(rawMessage: any) {
    this.timestamp = rawMessage.timestamp ? new Date(rawMessage.timestamp) : null;
    this.lines = rawMessage.lines;
  }

}

// used for mapping of incoming messages
export const WebsocketMessageMapping = {
  'TestMessage': TestMessage,
  'DeploymentStatusChangedMessage': DeploymentStatusChangedMessage,
  'ActivityMessage': ActivityMessage,
  'LogsMessage': LogsMessage
};

