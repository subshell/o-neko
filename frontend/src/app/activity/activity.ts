import * as moment from "moment";

export type PRIORITY = 'INFO' | 'WARNING' | 'ERROR';
export type ENTITY_TYPE = 'Project' | 'DockerRegistry' | 'User' | 'Namespace' | 'ProjectMesh';
export type CHANGE_TYPE = 'Saved' | 'Deleted';

export interface ActivityDTO {
  id: string;
  date: Date;
  priority: PRIORITY
  description: string;
  activityType: string;

  triggerName: string;
  triggerType: string

  entityId: string;
  entityName: string
  entityType: ENTITY_TYPE;
  changeType: CHANGE_TYPE;
  changedProperties?: string[]
}

export class Activity implements ActivityDTO {

  public id: string;
  public date: Date;
  public priority: PRIORITY;
  public description: string;
  public activityType: string;

  public triggerName: string;
  public triggerType: string;

  public entityId: string;
  public entityName: string;
  public entityType: ENTITY_TYPE;
  public changeType: CHANGE_TYPE;
  public changedProperties?: string[];

  constructor(dto: ActivityDTO) {
    this.id = dto.id;
    this.date = new Date(dto.date);
    this.priority = dto.priority;
    this.description = dto.description;
    this.activityType = dto.activityType;
    this.triggerName = dto.triggerName;
    this.triggerType = dto.triggerType;
    this.entityId = dto.entityId;
    this.entityName = dto.entityName;
    this.entityType = dto.entityType;
    this.changeType = dto.changeType;
    this.changedProperties = dto.changedProperties;
  }

  get formattedDate(): string {
    let year = this.date.getFullYear();
    let now = new Date();

    let mom = moment(this.date);
    if (now.getDate() === this.date.getDate()) {
      return mom.format('HH:mm:ss');
    } else if (now.getFullYear() === year) {
      return mom.format('Do MMM HH:mm:ss');
    }
    return mom.format('DD/MM/YY HH:mm:ss');
  }
}
