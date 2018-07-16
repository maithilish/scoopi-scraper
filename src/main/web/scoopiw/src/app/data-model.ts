import { isUndefined } from 'util';

export class Metric {
    name: string;
    type: string;
    clz: string;
    cat: string;
    label: string;
    // additional dynamic properties
}

export const nameLabelMap = [
    { name: 'JSoupHtmlParser.data.parse', label: 'Parse' },
    { name: 'JSoupHtmlParser.data.reuse', label: 'Reuse data' },
    { name: 'ParserCache.parser.cache.hit', label: 'Parse cache hit' },
    { name: 'ParserCache.parser.cache.miss', label: 'Parse cache miss' },
    { name: 'URLLoader.fetch.web', label: 'Web fetch ' },
    { name: 'LocatorSeeder.locator.provided', label: 'Provided by user' },
    { name: 'LocatorSeeder.locator.parsed', label: 'Parsed from pages' },
    { name: 'LocatorSeeder.locator.seeded', label: 'Pushed to queue' },
    { name: 'DataAppender.task.time', label: 'Data appender' },
    { name: 'DataConverter.task.time', label: 'Data converter' },
    { name: 'DataFilter.task.time', label: 'Data filter' },
    { name: 'JSoupHtmlParser.task.time', label: 'Parser' },
    { name: 'LocatorCreator.task.time', label: 'Locator creator' },
    { name: 'LocatorSeeder.task.time', label: 'Locator seeder' },
    { name: 'URLLoader.task.time', label: 'Document fetch and load' },
    { name: 'uptime', label: 'up time' },
    { name: 'systemLoad', label: 'Load average' },
    { name: 'totalMemory', label: 'Total memory' },
    { name: 'maxMemory', label: 'Max Memory' },
    { name: 'freeMemory', label: 'Free Memory' },
    { name: 'AppenderPoolService.pool.appender', label: 'Appender AppenderPool' },
    { name: 'TaskPoolService.pool.appender', label: 'Appender TaskPool' },
    { name: 'TaskPoolService.pool.converter', label: 'Converter TaskPool' },
    { name: 'TaskPoolService.pool.loader', label: 'Loader TaskPool' },
    { name: 'TaskPoolService.pool.parser', label: 'Parser TaskPool' },
    { name: 'TaskPoolService.pool.process', label: 'Process TaskPool' },
    { name: 'TaskPoolService.pool.seeder', label: 'Seeder TaskPool' },
];

export class MetricDataConverter {

    // convert metrics to metric data model
    convertMetrics(inMetrics: any): Array<Metric> {
        const metrics = Array<Metric>();
        Array.prototype.push.apply(metrics, this.convert('counter', inMetrics.counters));
        Array.prototype.push.apply(metrics, this.convert('meter', inMetrics.meters));
        Array.prototype.push.apply(metrics, this.convert('timer', inMetrics.timers));
        Array.prototype.push.apply(metrics, this.convert('histogram', inMetrics.histograms));
        Array.prototype.push.apply(metrics, this.convertGauges(inMetrics.gauges));
        return metrics;
    }

    convert(type: string, inMetrics: Array<any>): Array<Metric> {
        const metrics = Array<Metric>();
        Object.entries(inMetrics).forEach(inMetric => {
            const metricName = inMetric[0];
            const [clz, ...others] = metricName.split('.');
            const last = others.pop();
            const metricCat = others.join('.');
            // create metric
            const metric: Metric = {
                name: metricName,
                type: type,
                clz: clz,
                cat: metricCat,
                label: this.getLabel(metricName),
            };
            // add metric data as dynamic properties
            Object.entries(inMetric[1]).forEach(item => {
                metric[item[0]] = item[1];
            });
            metrics.push(metric);
        });
        return metrics;
    }

    /* metric gauge structure is different from others, so separate
       method to convert them */
    convertGauges(inMetrics: Array<any>): Array<Metric> {
        const metrics = Array<Metric>();

        Object.entries(inMetrics).forEach(inMetric => {
            const metricName = inMetric[0];
            const [clz, ...others] = metricName.split('.');
            const last = others.pop();
            const metricCat = others.join('.');
            Object.entries(inMetric[1]).forEach(value => {
                const items = value[1];
                // create metric
                const metric: Metric = {
                    name: metricName,
                    type: 'gauge',
                    clz: clz,
                    cat: metricCat,
                    label: this.getLabel(metricName),
                };
                // create and add data
                Object.entries(items).forEach(item => {
                    metric[item[0]] = item[1];
                });
                metrics.push(metric);
            });
        });
        return metrics;
    }

    getLabel(name: string): string {
        const item = nameLabelMap.find(ii => {
            if (ii.name === name) {
                return true;
            }
        });
        if (isUndefined(item)) {
            return name;
        } else {
            return item.label;
        }
    }
}
