import { Component, Input, OnChanges } from '@angular/core';
import { Metric } from '../data-model';

@Component({
  selector: 'app-meter-panel',
  templateUrl: './meter-panel.component.html',
  styleUrls: ['./meter-panel.component.css']
})
export class MeterPanelComponent implements OnChanges {

  @Input() metrics!: Metric[];
  @Input() cat!: string;
  @Input() heading!: string;

  meters!: Metric[];

  constructor() { }

  ngOnChanges() {
    this.filterData();
  }

  filterData() {
    this.meters = this.metrics.filter(metric => {
      if (metric.type === 'meter' && metric.cat === this.cat) {
        return metric;
      } else {
        return undefined;
      }
    });
  }

}
