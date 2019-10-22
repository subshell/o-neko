import * as moment from "moment";

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
    if (this.timestamp === null) {
      return null;
    }

    let now = new Date();
    let timestampMoment = moment(this.timestamp);
    if (moment(now).isSame(timestampMoment, 'day')) {
      return timestampMoment.format('HH:mm:ss');
    } else if (moment(now).isSame(timestampMoment, 'month')) {
      return timestampMoment.format('Do MMM HH:mm:ss');
    }
    return timestampMoment.format('DD/MM/YY HH:mm:ss');
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
