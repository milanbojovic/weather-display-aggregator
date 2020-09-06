import { Component } from '@angular/core';

@Component({
  selector: 'app-root',
  templateUrl: './app.component.html',
  styleUrls: ['./app.component.css']
})
export class AppComponent {
  providerSource = 'accu';
  currentCity = 'beograd';

  setProviderSource(id: string): void {
    this.providerSource = id;
  }

  setCity(city: string): void{
      this.currentCity = city;
  }
}
