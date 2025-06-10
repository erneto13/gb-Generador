import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable, BehaviorSubject, Observer, tap } from 'rxjs';
import { Rectangulo } from '../../../interfaces/Rectangulo';

@Injectable({
providedIn: 'root'
})

export class RectanguloService {

    constructor(
    private http: HttpClient
    ) { }

    private baseUrl = "http://localhost:8080/api/";

    getAllRectangulo(): Observable<Rectangulo[]> {
         return this.http.get<Rectangulo[]>(this.baseUrl);
    }
    createRectangulo (rectangulo: Rectangulo): Observable<any> {
        return this.http.post<Rectangulo>(this.baseUrl, rectangulo);
    }
    getRectanguloById(id: number): Observable<Rectangulo> {
        return this.http.get<Rectangulo>(`${this.baseUrl}/${id}`);
    }
    updateRectangulo (rectangulo: Rectangulo, id: number): Observable<any> {
        return this.http.put<Rectangulo>(`${this.baseUrl}/${id}`, rectangulo);
    }
    deleteRectangulo (id: number): Observable<any> {
        return this.http.delete<Rectangulo>(`${this.baseUrl}/${id}`);
    }
}