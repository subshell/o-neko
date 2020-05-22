import {relativeDateString} from "../util/date-time-parser";

export enum DesiredState {
  Deployed = "Deployed",
  NotDeployed = "NotDeployed"
}

export enum DeployableStatus {
  Pending = "Pending",
  Running = "Running",
  Succeeded = "Succeeded",
  Failed = "Failed",
  Unknown = "Unknown",
  NotScheduled = "NotScheduled"
}

export interface DeploymentDTO {
  status: DeployableStatus
  timestamp: string | Date
  containerCount: number
  readyContainerCount: number
}

export interface DeploymentUpdate {
  status: DeployableStatus;
  timestamp: Date;
}

export class Deployment implements DeploymentDTO {
  status: DeployableStatus;
  timestamp: Date;
  containerCount: number;
  readyContainerCount: number;

  get formattedTimestamp(): string {
    return relativeDateString(this.timestamp);
  }

  static from(from: DeploymentDTO): Deployment {
    let deployment = new Deployment();
    deployment.status = from.status;
    deployment.timestamp = from.timestamp ? new Date(from.timestamp as string) : null;
    deployment.containerCount = from.containerCount;
    deployment.readyContainerCount = from.readyContainerCount;
    return deployment;
  }

  public updateWith(update: DeploymentUpdate) {
    this.status = update.status;
    this.timestamp = update.timestamp != null ? update.timestamp : null;
  }
}
