import { InMemoryDbService } from 'angular-in-memory-web-api';

export class InMemoryDataService implements InMemoryDbService {
    createDb() {
        const metrics = {
            'version': '3.0.0',
            'gauges': {
                'GSystem.system.stats': {
                    'value': {
                        'uptime': '0:0:23',
                        'systemLoad': 2.34,
                        'maxMemory': 860,
                        'totalMemory': 338,
                        'freeMemory': 114
                    }
                },
                'TaskPoolService.pool.converter': {
                    'value': {
                        'activeCount': 4,
                        'poolSize': 4,
                        'completedTaskCount': 153,
                        'taskCount': 234
                    }
                },
                'TaskPoolService.pool.loader': {
                    'value': {
                        'activeCount': 1,
                        'poolSize': 4,
                        'completedTaskCount': 473,
                        'taskCount': 474
                    }
                },
                'TaskPoolService.pool.parser': {
                    'value': {
                        'activeCount': 4,
                        'poolSize': 4,
                        'completedTaskCount': 427,
                        'taskCount': 833
                    }
                },
                'TaskPoolService.pool.process': {
                    'value': {
                        'activeCount': 12,
                        'poolSize': 12,
                        'completedTaskCount': 395,
                        'taskCount': 427
                    }
                },
                'TaskPoolService.pool.seeder': {
                    'value': {
                        'activeCount': 2,
                        'poolSize': 6,
                        'completedTaskCount': 384,
                        'taskCount': 386
                    }
                }
            },
            'counters': {
                'JSoupHtmlParser.data.parse': {
                    'count': 0
                },
                'JSoupHtmlParser.data.reuse': {
                    'count': 430
                },
                'Task.system.error': {
                    'count': 2
                },
                'PageLoader.fetch.web': {
                    'count': 30
                },
                'ParserCache.parser.cache.hit': {
                    'count': 100000
                },
                'ParserCache.parser.cache.miss': {
                    'count': 90010
                },
            },
            'histograms': {},
            'meters': {
                'LocatorSeeder.locator.parsed': {
                    'count': 385,
                    'm15_rate': 2.8950643663095086,
                    'm1_rate': 2.4851156829967573,
                    'm5_rate': 2.7388188403288356,
                    'mean_rate': 2.349800776753161,
                    'units': 'events/second'
                },
                'LocatorSeeder.locator.provided': {
                    'count': 90,
                    'm15_rate': 15.068311764493735,
                    'm1_rate': 1.2507021220104289,
                    'm5_rate': 10.559631951180569,
                    'mean_rate': 0.5382523604976951,
                    'units': 'events/second'
                },
                'LocatorSeeder.locator.seeded': {
                    'count': 475,
                    'm15_rate': 2.4770266193138877,
                    'm1_rate': 2.67400745089201,
                    'm5_rate': 2.5836670994504827,
                    'mean_rate': 2.8732732989442695,
                    'units': 'events/second'
                }
            },
            'timers': {
                'DataAppender.task.time': {
                    'count': 153,
                    'max': 0.5480012860000001,
                    'mean': 0.1429107209071955,
                    'min': 0.011499445,
                    'p50': 0.133345855,
                    'p75': 0.19439930400000002,
                    'p95': 0.30011981400000004,
                    'p98': 0.35952216800000003,
                    'p99': 0.398491244,
                    'p999': 0.5480012860000001,
                    'stddev': 0.08729969654856884,
                    'm15_rate': 1.9945665621850563,
                    'm1_rate': 0.99847059500312,
                    'm5_rate': 1.6750555776149891,
                    'mean_rate': 0.9354911144121281,
                    'duration_units': 'seconds',
                    'rate_units': 'calls/second'
                },
                'DataConverter.task.time': {
                    'count': 153,
                    'max': 31.018041070000002,
                    'mean': 3.9971781017301646,
                    'min': 0.038969326000000006,
                    'p50': 0.509941807,
                    'p75': 1.641571936,
                    'p95': 23.195443715,
                    'p98': 27.946267362,
                    'p99': 29.043188485,
                    'p999': 31.018041070000002,
                    'stddev': 7.628971915939437,
                    'm15_rate': 1.9945900651474942,
                    'm1_rate': 1.0014990230774583,
                    'm5_rate': 1.6752480044996847,
                    'mean_rate': 0.9329759924268358,
                    'duration_units': 'seconds',
                    'rate_units': 'calls/second'
                },
                'DataFilter.task.time': {
                    'count': 235,
                    'max': 68.603909645,
                    'mean': 4.465407281237694,
                    'min': 0.004677280000000001,
                    'p50': 0.104481664,
                    'p75': 0.23672025400000002,
                    'p95': 28.554043715000002,
                    'p98': 63.601975395000004,
                    'p99': 64.41907130300001,
                    'p999': 68.603909645,
                    'stddev': 12.011880973769458,
                    'm15_rate': 2.076647195895668,
                    'm1_rate': 1.57011377190037,
                    'm5_rate': 1.8914801547799243,
                    'mean_rate': 1.4313915359795075,
                    'duration_units': 'seconds',
                    'rate_units': 'calls/second'
                },
                'JSoupHtmlParser.task.time': {
                    'count': 428,
                    'max': 18.344952221,
                    'mean': 1.4485385707357452,
                    'min': 0.20897985000000002,
                    'p50': 0.552331529,
                    'p75': 0.8653084700000001,
                    'p95': 8.873506793,
                    'p98': 10.893871867000001,
                    'p99': 11.340225164000001,
                    'p999': 18.344952221,
                    'stddev': 2.683195875184416,
                    'm15_rate': 3.9343009297688596,
                    'm1_rate': 2.817576703112432,
                    'm5_rate': 3.5317070604165846,
                    'mean_rate': 2.598745731298,
                    'duration_units': 'seconds',
                    'rate_units': 'calls/second'
                },
                'LocatorCreator.task.time': {
                    'count': 161,
                    'max': 8.562054143000001,
                    'mean': 3.6045532665498694,
                    'min': 1.184132444,
                    'p50': 1.623177651,
                    'p75': 6.94949548,
                    'p95': 7.6576301440000005,
                    'p98': 7.794584738,
                    'p99': 8.227201101,
                    'p999': 8.562054143000001,
                    'stddev': 2.774893415559728,
                    'm15_rate': 0.6622868793013246,
                    'm1_rate': 0.9746367287443733,
                    'm5_rate': 0.7591642491168625,
                    'mean_rate': 0.9803524797198339,
                    'duration_units': 'seconds',
                    'rate_units': 'calls/second'
                },
                'LocatorSeeder.task.time': {
                    'count': 384,
                    'max': 99.066459311,
                    'mean': 1.4588415715024081,
                    'min': 1.057454488,
                    'p50': 1.212498158,
                    'p75': 1.288480987,
                    'p95': 1.418836618,
                    'p98': 1.4751619610000002,
                    'p99': 1.483124888,
                    'p999': 99.066459311,
                    'stddev': 4.782036760305924,
                    'm15_rate': 0.7186602298296406,
                    'm1_rate': 2.309771156676652,
                    'm5_rate': 1.2144381422713082,
                    'mean_rate': 2.2939279125967618,
                    'duration_units': 'seconds',
                    'rate_units': 'calls/second'
                },
                'PageLoader.task.time': {
                    'count': 474,
                    'max': 2.5054812920000002,
                    'mean': 0.7541652511767615,
                    'min': 0.24120517900000002,
                    'p50': 0.6821511610000001,
                    'p75': 0.8146983050000001,
                    'p95': 1.475461689,
                    'p98': 1.714787292,
                    'p99': 1.877715491,
                    'p999': 1.9968276010000001,
                    'stddev': 0.30565811405734594,
                    'm15_rate': 1.9780400337974433,
                    'm1_rate': 2.6675609863509817,
                    'm5_rate': 2.241180683917608,
                    'mean_rate': 2.866326136394105,
                    'duration_units': 'seconds',
                    'rate_units': 'calls/second'
                }
            }
        };
        return { metrics };
    }
}
