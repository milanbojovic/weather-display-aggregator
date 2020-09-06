import {Component, Input, OnInit, SimpleChanges} from '@angular/core';
import {HttpClient} from '@angular/common/http';
import {WeatherData} from '../../model/weather-data.model';
import { Observable, forkJoin } from 'rxjs';
import {ForecastedWeather} from "../../model/forecasted-weather.model";


@Component({
  selector: 'weather-component',
  templateUrl: './weather-component.component.html',
  styleUrls: ['./weather-component.component.css']
})
export class WeatherComponent implements OnInit {

    constructor(private http: HttpClient) { }

    @Input()
    customTitle: string;

    weatherData: WeatherData = null;
    combinedWeatherData: WeatherData[] = null;
    combinedForecastedData: Array<ForecastedWeather> = [];

    ngOnInit(): void {
        this.customTitle = 'accu';
        const apiUrl = 'http://localhost:8080/' + this.customTitle;
        const test = this.http.get(apiUrl)
          .subscribe((data: WeatherData) => {
            this.weatherData = data;
        });
    }

    ngOnChanges(changes: SimpleChanges) {
        const apiUrl = 'http://localhost:8080/' + this.customTitle;
        if (this.customTitle === 'combined') {
          const accuw: Observable <WeatherData> = this.http.get<WeatherData>('http://localhost:8080/accu');
          const w2u: Observable <WeatherData> = this.http.get<WeatherData>('http://localhost:8080/w2u');
          const rhmz: Observable<WeatherData> = this.http.get<WeatherData>('http://localhost:8080/rhmz');

          forkJoin([accuw, w2u, rhmz]).subscribe(combinedResults  => {
            this.combinedWeatherData = combinedResults;
            this.combinedForecastedData = this.extractForecastLists(combinedResults);
            console.log('console logging debug');
          });
        } else {
          this.http.get(apiUrl).subscribe((data: WeatherData) => {
              this.weatherData = data;
            });
        }
    }

  private extractForecastLists(data: WeatherData[]) {
    let resultArray = Array<ForecastedWeather>();
    for (var i = 0; i < 4; i++) {
      const provider: WeatherData = data[i];
      for (var j = 0; j < 3; j++) {
        resultArray.push(data[j].weeklyForecast[i]);
      }
    }
    return resultArray;
  }

  getUrl(provider: string): string {
    switch (provider) {
      case 'accu': {
        return 'https://www.accuweather.com/';
        break;
      }
      case 'w2u': {
        return 'https://www.weather2umbrella.com/';
        break;
      }
      case 'rhmz': {
        return 'http://www.hidmet.gov.rs/';
        break;
      }
      case 'combined': {
        return 'http://www.hidmet.gov.rs/';
        break;
      }
    }
  }
}
