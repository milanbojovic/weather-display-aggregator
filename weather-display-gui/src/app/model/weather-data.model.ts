import {ForecastedWeather} from './forecasted-weather.model';
import {CurrentWeather} from './current-weather.model';

export class WeatherData {
  city: string;
  currentWeather: CurrentWeather;
  weeklyForecast: ForecastedWeather[];
}
