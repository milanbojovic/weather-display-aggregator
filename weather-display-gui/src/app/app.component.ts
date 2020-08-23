import { Component } from '@angular/core';

@Component({
  selector: 'app-root',
  templateUrl: './app.component.html',
  styleUrls: ['./app.component.css']
})
export class AppComponent {
  title = 'weather-display-gui';
  activeWeather = 'aque';

    passTheSalt(id){
            this.activeWeather = id;
            console.log(id);
       }
}
