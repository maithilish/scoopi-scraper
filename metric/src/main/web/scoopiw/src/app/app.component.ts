import { Observable, of, timer } from 'rxjs';
import { Component, OnInit } from '@angular/core';
import { DataService } from './data.service';
import { MetricDataConverter, Metric } from './data-model';
import { timeInterval } from 'rxjs/operators';

/* import { TimeInterval } from 'rxjs/internal/operators/timeInterval'; */

@Component({
  selector: 'app-root',
  templateUrl: './app.component.html',
  styleUrls: ['./app.component.css']
})
export class AppComponent implements OnInit {

  title: string;
  logo = 'assets/images/gotz-logo.png';
  success = true;

  metrics: Metric[] = [];

  constructor(private dataService: DataService) {
    this.title = 'Scoopi Metrics';
  }

  ngOnInit() {
    this.getMetrics();
  }

  getMetrics(): void {
    const timerInterval = timer(0, 3000);
    timerInterval.subscribe(t => {
      this.dataService.getMetrics()
        .subscribe(inMetrics => {
          if (inMetrics !== undefined) {
            const converter = new MetricDataConverter();
            this.metrics = converter.convertMetrics(inMetrics);
          }
          this.success = this.dataService.getSuccess();
        });
    });
  }
}
