import { Component, Input, OnChanges } from '@angular/core';
import { Metric } from '../data-model';


@Component({
  selector: 'app-timer-panel',
  templateUrl: './timer-panel.component.html',
  styleUrls: ['./timer-panel.component.css']
})
export class TimerPanelComponent implements OnChanges {

  @Input() metrics!: Metric[];
  @Input() cat!: string;
  @Input() heading!: string;

  timers!: Metric[];

  constructor() { }

  ngOnChanges() {
    this.filterData();
  }

  filterData() {
    this.timers = this.metrics.filter(metric => {
      if (metric.type === 'timer' && metric.cat === this.cat) {
        return metric;
      } else {
        return undefined;
      }
    });
  }
}
