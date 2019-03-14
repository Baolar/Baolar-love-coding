#pragma once
#ifndef _HEADERNAME_H
#define _HEADERNAME_H
#include <vector>
#include <utility>
#include "string_ext.h"
#include <cstdio> //for test
#define MAXN 20
#define INF 100000 //INT_MAX

using namespace std;

//设计一个结构用来储存边结点
struct ArcNode { //边结点
    int nextplace; //指向下一个结点
    ArcNode *nextarc; //指向下一个边结点
    int distance; //边的权重 两个景点之间的距离
    ArcNode(){
        nextplace = distance = 0;
        nextarc = NULL;
    }
};

//结点
struct Place {
    ArcNode *firstarc;
    Place() {
        firstarc = NULL;
    }
};

//领接表
class AdjGraph {
private:
    Place *places;
    int n;  //边数
    int e;	//景点个数
    int A[MAXN][MAXN];
    void Simpath(int *path, bool *visited, int st, int en, int p, string &ans);
    string Simpath(int st, int en);
    void Prim(vector<pair<int, int>> &t);
    void Dijkstra(int v, int *dist, int *path, int *S);
    string Dispath2(int A[][MAXN],int path[][MAXN], int st, int en);
public:
    AdjGraph();
    ~AdjGraph();
    void CreateAdj(int A[MAXN][MAXN], int n, int e);//用邻接矩阵创建领接表
    string TravelAll(int st);
    string MSTree();
    void Dispath(int *dist, int *path, int *S, int v);
    string Floyd(int st,int en);
};
#endif