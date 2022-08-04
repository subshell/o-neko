import {environment} from "../../environments/environment";
import {Class} from "./class";
import {StringUtils} from "./string-utils";
import {LogLevel} from "typescript-logging";
import {Log4TSProvider, Logger} from "typescript-logging-log4ts-style";

export class LogService {

  private provider: Log4TSProvider;

  constructor() {
    this.provider = Log4TSProvider.createProvider("ONekoLogProvider", {
      groups: [{
        level: environment.production ? LogLevel.Debug : LogLevel.Trace,
        expression: new RegExp(".+")
      }]
    });
    LogService._instance = this;
  }

  private static _instance: LogService;

  /**
   * @returns {LogService}
   */
  private static get instance(): LogService {
    if (!LogService._instance) {
      LogService._instance = new LogService();
    }
    return LogService._instance;
  }

  /**
   * Creates a logger named based on the parameters passed in.
   *
   * @param {Class<T>} forClass The class this logger is going to be used in.
   * @param {string} namePrefix A prefix you might want to prepend to the logger name.
   * @param {string} nameSuffix A suffix you might want to append to the logger name.
   * @param {string} fallbackName A name just in case the name could not be extracted from the class.
   * @returns {Logger}
   */
  public static getLogger<T>(forClass: Class<T>, namePrefix?: string, nameSuffix?: string, fallbackName?: string): Logger {
    let className = (<any>forClass).name;
    let name = `${namePrefix ? namePrefix : ''}${!StringUtils.isEmpty(className) ? className : fallbackName}${nameSuffix ? nameSuffix : ''}`;
    if (!StringUtils.isEmpty(name)) {
      return LogService.instance.provider.getLogger(name);
    } else {
      throw new Error("Could not create logger.");
    }
  }

  /**
   * @deprecated Should only be used when there is no class to pass into getLogger(...)
   *
   * Creates a logger with a custom name.
   *
   * @param {string} name
   * @returns {Logger}
   */
  public static getLoggerWithName(name: string): Logger {
    return LogService.instance.provider.getLogger(name);
  }
}
