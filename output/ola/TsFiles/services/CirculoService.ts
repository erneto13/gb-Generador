import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable, BehaviorSubject, Observer, tap } from 'rxjs';
import { Circulo } from '../../../interfaces/Circulo';

@Injectable({
providedIn: 'root'
})

export class CirculoService {

    constructor(
    private http: HttpClient
    ) { }

    private baseUrl = "http://localhost:8080/api/";

    getAllCirculo(): Observable<Circulo[]> {
         return this.http.get<Circulo[]>(this.baseUrl);
    }
    createCirculo (circulo: Circulo): Observable<any> {
        return this.http.post<Circulo>(this.baseUrl, circulo);
    }
    getCirculoById(id: number): Observable<Circulo> {
        return this.http.get<Circulo>(`${this.baseUrl}/${id}`);
    }
    updateCirculo (circulo: Circulo, id: number): Observable<any> {
        return this.http.put<Circulo>(`${this.baseUrl}/${id}`, circulo);
    }
    deleteCirculo (id: number): Observable<any> {
        return this.http.delete<Circulo>(`${this.baseUrl}/${id}`);
    }
}