import { Injectable } from '@angular/core';
import { HttpClient, HttpHeaders } from '@angular/common/http';

import { Observable, of, interval } from 'rxjs';
import { catchError, map, tap } from 'rxjs/operators';

import { Metric } from './data-model';

@Injectable()
export class DataService {

    private metricUrl = 'http://localhost:9010/api/metrics';

    success = true;

    constructor(private http: HttpClient) { }

    getMetrics(): Observable<any> {
        this.success = true;
        return this.http.get<any>(this.metricUrl)
            .pipe(
                tap(x => console.log(`fetched metrics`)),
                catchError(this.handleError('getMetrics'))
            );
    }

    private handleError<T>(operation = 'operation', result?: T) {
        return (error: any): Observable<T> => {
            console.error(error);
            console.log(`${operation} failed: ${error.message}`);
            // keep app running, return an empty result
            this.success = false;
            return of(result as T);
        };
    }

    getSuccess() {
        return this.success;
    }

}

