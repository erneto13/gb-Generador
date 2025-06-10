import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable, BehaviorSubject, Observer, tap } from 'rxjs';
import { Ubicacion } from '../../../interfaces/Ubicacion';

@Injectable({
providedIn: 'root'
})

export class UbicacionService {

    constructor(
    private http: HttpClient
    ) { }

    private baseUrl = "http://localhost:8080/api/";

    getAllUbicacion(): Observable<Ubicacion[]> {
         return this.http.get<Ubicacion[]>(this.baseUrl);
    }
    createUbicacion (ubicacion: Ubicacion): Observable<any> {
        return this.http.post<Ubicacion>(this.baseUrl, ubicacion);
    }
    getUbicacionById(id: number): Observable<Ubicacion> {
        return this.http.get<Ubicacion>(`${this.baseUrl}/${id}`);
    }
    updateUbicacion (ubicacion: Ubicacion, id: number): Observable<any> {
        return this.http.put<Ubicacion>(`${this.baseUrl}/${id}`, ubicacion);
    }
    deleteUbicacion (id: number): Observable<any> {
        return this.http.delete<Ubicacion>(`${this.baseUrl}/${id}`);
    }
}