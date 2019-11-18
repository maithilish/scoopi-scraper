import { DataService } from './data.service';
import { InMemoryDataService } from './in-mem-data.service';
import { BrowserModule } from '@angular/platform-browser';
import { NgModule } from '@angular/core';

import { AppComponent } from './app.component';
import { HttpClientInMemoryWebApiModule } from 'angular-in-memory-web-api';
import { HttpClientModule } from '@angular/common/http';
import { PoolPanelComponent } from './pool-panel/pool-panel.component';
import { environment } from '../environments/environment';
import { CounterPanelComponent } from './counter-panel/counter-panel.component';
import { MeterPanelComponent } from './meter-panel/meter-panel.component';
import { TimerPanelComponent } from './timer-panel/timer-panel.component';
import { SysStatPanelComponent } from './sys-stat-panel/sys-stat-panel.component';

@NgModule({
  declarations: [
    AppComponent,
    PoolPanelComponent,
    CounterPanelComponent,
    MeterPanelComponent,
    TimerPanelComponent,
    SysStatPanelComponent
],
  imports: [
    BrowserModule,
    HttpClientModule,
    environment.production ?
      [] : HttpClientInMemoryWebApiModule.forRoot(InMemoryDataService)
  ],
  providers: [DataService],
  bootstrap: [AppComponent]
})
export class AppModule { }
