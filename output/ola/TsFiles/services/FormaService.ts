import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable, BehaviorSubject, Observer, tap } from 'rxjs';
import { Forma } from '../../../interfaces/Forma';

@Injectable({
providedIn: 'root'
})

export class FormaService {

    constructor(
    private http: HttpClient
    ) { }

    private baseUrl = "http://localhost:8080/api/";

    getAllForma(): Observable<Forma[]> {
         return this.http.get<Forma[]>(this.baseUrl);
    }
    createForma (forma: Forma): Observable<any> {
        return this.http.post<Forma>(this.baseUrl, forma);
    }
    getFormaById(id: number): Observable<Forma> {
        return this.http.get<Forma>(`${this.baseUrl}/${id}`);
    }
    updateForma (forma: Forma, id: number): Observable<any> {
        return this.http.put<Forma>(`${this.baseUrl}/${id}`, forma);
    }
    deleteForma (id: number): Observable<any> {
        return this.http.delete<Forma>(`${this.baseUrl}/${id}`);
    }
}