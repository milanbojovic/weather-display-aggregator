import {Component, Input, OnInit, SimpleChanges} from '@angular/core';
import {HttpClient} from '@angular/common/http';
import {WeatherData} from '../../model/weather-data.model';
import { Observable, forkJoin } from 'rxjs';
import {ForecastedWeather} from '../../model/forecasted-weather.model';


@Component({
  selector: 'weather-component',
  templateUrl: './weather-component.component.html',
  styleUrls: ['./weather-component.component.css']
})
export class WeatherComponent implements OnInit {

    constructor(private http: HttpClient) { }

    @Input() providerSource: string;
    @Input() city: string;

    weatherData: WeatherData = null;
    combinedWeatherData: WeatherData[] = null;
    combinedForecastedData: Array<ForecastedWeather> = [];

  private readonly apiUrl = 'http://localhost:8080/';
  private readonly apiRhmz = this.apiUrl + 'rhmz/';
  private readonly apiW2u = this.apiUrl + 'w2u/';
  private readonly apiAccu = this.apiUrl + 'accu/';

  ngOnInit(): void {
        this.providerSource = 'accu';
        this.city = 'beograd';
        const test = this.http.get(this.assembleCurrentlySelectedApiUrl())
          .subscribe((data: WeatherData) => {
            this.weatherData = data;
        });
    }

  private assembleCurrentlySelectedApiUrl(): string {
    return this.apiUrl + this.providerSource + '/' + this.city;
  }

  ngOnChanges(changes: SimpleChanges) {
        if (this.providerSource === 'combined') {
          const accuw: Observable <WeatherData> = this.http.get<WeatherData>(this.apiAccu + this.city);
          const w2u: Observable <WeatherData> = this.http.get<WeatherData>(this.apiW2u + this.city);
          const rhmz: Observable<WeatherData> = this.http.get<WeatherData>(this.apiAccu + this.city);

          forkJoin([accuw, w2u, rhmz]).subscribe(combinedResults  => {
            this.combinedWeatherData = combinedResults;
            this.combinedForecastedData = this.extractForecastLists(combinedResults);
            console.log('console logging debug');
          });
        } else {
          this.http.get(this.assembleCurrentlySelectedApiUrl()).subscribe((data: WeatherData) => {
              this.weatherData = data;
            });
        }
    }

  private extractForecastLists(data: WeatherData[]): Array<ForecastedWeather> {
    const resultArray = Array<ForecastedWeather>();
    for (let i = 0; i < 4; i++) {
      const provider: WeatherData = data[i];
      for (let j = 0; j < 3; j++) {
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
