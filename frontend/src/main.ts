import {enableProdMode} from '@angular/core';
import {platformBrowserDynamic} from '@angular/platform-browser-dynamic';

import {AppModule} from './app/app.module';
import {LogService} from "./app/util/log.service";
import {environment} from './environments/environment';

const log = LogService.getLoggerWithName("main.ts");

if (environment.production) {
  enableProdMode();
}

platformBrowserDynamic().bootstrapModule(AppModule)
  .catch(err => log.error(err));
