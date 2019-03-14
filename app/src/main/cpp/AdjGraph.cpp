//
// Created by baola on 2019/2/28 0028.
//

#ifndef _HEADERNAME_H1
#define _HEADERNAME_H1
#include "AdjGraph.h"
#include "string_ext.h"
#include <vector>
#include <iostream>
using namespace std;

AdjGraph::AdjGraph() {
    places = NULL;
    n = e = 0;
}

AdjGraph::~AdjGraph() {
    for (int i = 0; i < e; i++) {
        ArcNode *temp = places[i].firstarc;
        while (temp) {
            ArcNode *temp2 = temp->nextarc;
            delete temp;
            temp = temp2;
        }
    }
    delete[]places;
}

void AdjGraph::CreateAdj(int A[MAXN][MAXN], int n, int e) {
    for (int i = 0; i < e; i++)
        for (int j = 0; j < e; j++)
            this->A[i][j] = A[i][j];

    places = new Place[e];
    this->e = e;
    this->n = n;
    for (int i = 0; i < e; i++) {
        for (int j = 0; j < e; j++) {
            if (A[i][j] != INF && A[i][j] != 0) {
                ArcNode *NewArcNode = new ArcNode();
                NewArcNode->nextplace = j;
                NewArcNode->distance = A[i][j];
                NewArcNode->nextarc = places[i].firstarc;
                places[i].firstarc = NewArcNode;
            }
        }
    }
}

string AdjGraph::TravelAll(int st) {
    string ans;
    for (int i = 0; i < e; i++)
        ans = ans + Simpath(st, i);
    return ans;
}

string AdjGraph::MSTree() {
    vector<pair<int, int>> stree;
    string ans;
    Prim(stree);
    for (int i = 0; i < stree.size(); i++)
        ans = ans + stree[i].first + "*" + stree[i].second + "*" + A[stree[i].first][stree[i].second] + "\n";
    //形式  int * int *int

    return ans;
}

string AdjGraph::Simpath(int st, int en) {
    int path[MAXN];
    bool visited[MAXN] = { 0 };
    string ans;
    Simpath(path, visited, st, en, -1, ans);

    return ans;
}

void AdjGraph::Prim(vector<pair<int, int>> &t) {
    int lowcost[MAXN], closest[MAXN];
    int v = 0;
    for (int i = 0; i < e; i++) {
        lowcost[i] = A[v][i];
        closest[i] = v;
    }

    for (int i = 1; i < e; i++) {
        int minweight = INF;
        int k;
        for (int j = 0; j < e; j++)
            if (lowcost[j] != 0 && lowcost[j] < minweight) {
                minweight = lowcost[j];
                k = j;
            }
        pair<int, int> adjtemp(closest[k], k);
        t.push_back(adjtemp);

        lowcost[k] = 0;
        for (int j = 0; j < e; j++)
            if (lowcost[j] != 0 && A[k][j] < lowcost[j]) {
                lowcost[j] = A[k][j];
                closest[j] = k;
            }
    }
}

void AdjGraph::Simpath(int *path, bool * visited, int st, int en, int p, string &ans) {
    visited[st] = 1;
    path[++p] = st;

    if (st == en) {
        if (p == e - 1) {
            for (int i = 0; i < p; i++) {
                int temp = path[i];
                ans = ans + path[i] + "*";
            }
            ans = ans + path[p] + "\n";
        }
    }
    ArcNode *temp = places[st].firstarc;
    while (temp) {
        if (!visited[temp->nextplace])
            Simpath(path, visited, temp->nextplace, en, p, ans);
        temp = temp->nextarc;
    }
    visited[st] = 0;
}

void AdjGraph::Dijkstra(int v, int *dist, int *path, int *S){
    cout << "display matrix before start:" << endl;
    for (int i = 0; i < e; i++)
        for (int j = 0; j < e; j++) {
            if (A[i][j] == INF)
                cout << "I" << (j == e - 1 ? "\n" : "\t");
            else
                cout << A[i][j] << (j == e - 1 ? "\n" : "\t");
        }

    int MINdis, u;
    for (int i = 0; i < e; i++) {
        dist[i] = A[v][i];
        S[i] = 0;
        if (A[v][i] < INF)
            path[i] = v;
        else
            path[i] = -1;
    }
    S[v] = 1;
    path[v] = 0;
    for (int i = 0; i < e - 1; i++) {
        MINdis = INF;
        for (int j = 0; j < e; j++) {
            if (S[j] == 0 && dist[j] < MINdis) {
                u = i;
                MINdis = dist[j];
            }
        }
        S[u] = 1;
        for (int j = 0; j < e; j++)
            if (S[j] == 0 && A[i][j] < INF&&dist[u] + A[u][j] < dist[j]) {
                dist[j] = dist[u] + A[u][j];
                path[j] = u;
            }
    }
    Dispath(dist, path, S, v);
}


void AdjGraph::Dispath(int * dist, int * path, int * S, int v) {
    int k;
    int apath[MAXN] = { 0 }, d;
    for(int i=0;i<e;i++)
        if (S[i] == 1 && i != v) {
            printf("从顶点%d到顶点%d的路径长度是:%d\t路径为:", v, i, dist[i]);
            d = 0;
            apath[d] = i;
            k = path[i];
            if (k == -1)
                printf("无路径\n");
            else {
                while (k != v) {
                    d++;
                    apath[d] = k;
                    k = path[k];
                }
                d++;
                apath[d] = v;
                printf("%d", apath[d]);
                for (int j = d - 1; j >= 0; j--)
                    printf(",%d", apath[j]);
                printf("\n");
            }
        }
}

string AdjGraph::Floyd(int st,int en) {
    int M[MAXN][MAXN] = { 0 }, path[MAXN][MAXN] = { 0 };
    for (int i = 0; i < e; i++) {
        for (int j = 0; j < e; j++) {
            M[i][j] = A[i][j];
            if (i != j && A[i][j] < INF)
                path[i][j] = i;
            else
                path[i][j] = -1;
        }
    }

    for (int k = 0; k < e; k++) {
        for(int i=0;i<e;i++)
            for(int j=0;j<e;j++)
                if (M[i][j] > M[i][k] + M[k][j]) {
                    M[i][j] = M[i][k] + M[k][j];
                    path[i][j] = path[k][j];
                }
    }

    return Dispath2(M, path , st, en);
}

string AdjGraph::Dispath2(int A[][MAXN], int path[][MAXN],int st,int en) {
    string ans = "";
    int apath[MAXN], d;
    for(int i=0;i<e;i++)
        for (int j = 0; j < e; j++) {
            if (A[i][j] != INF && i != j) {
                if (i == st && j == en){
                    //printf(" 从%d到%d的路径为:", i, j);
                    int k = path[i][j];
                    d = 0;
                    apath[d] = j;
                    while (k != -1 && k != i) {
                        d++;
                        apath[d] = k;
                        k = path[i][k];
                    }
                    d++; apath[d] = i;
                    printf("%d", apath[d]);
                    ans = ans + apath[d];
                    for (int s = d - 1; s >= 0; s--) {
                        printf(",%d", apath[s]);
                        ans = ans + "*" + apath[s];

                    }
                    ans = ans + "\n" + A[i][j];
                    printf("\t路径长度为:%d\n", A[i][j]);
                    break;
                }
            }
        }

    return ans;
}
#endif