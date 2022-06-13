import {relativeDateString} from "../util/date-time-parser";

export type PRIORITY = 'INFO' | 'WARNING' | 'ERROR';
export type ENTITY_TYPE = 'Project' | 'DockerRegistry' | 'User' | 'Namespace';
export type CHANGE_TYPE = 'Saved' | 'Deleted';

export interface ActivityDTO {
  id: string;
  date: Date;
  priority: PRIORITY;
  name: string;
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
  public name: string;
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
    this.name = dto.name;
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
    return relativeDateString(this.date);
  }
}
