#include <jni.h>
#include "AdjGraph.h"
#include "string_ext.h"
#include "AdjGraph.cpp"
#include <iostream>
#include <cstring>

void Init(const char *A, int M[MAXN][MAXN]) {
    int i = 0, j = 0;
    for (int t = 0; t < strlen(A); t++) {
        switch (A[t]) {
            case',':j++; break;
            case'\n':i++; j = 0; break;
            case'I':M[i][j] = INF; break;
            default:
                int st = t;
                while (A[t] != ','&&A[t] != '\n') t++;

                char str[1000] = { 0 };
                int p = 0;
                for (int s = st; s < t; s++)
                    str[p++] = A[s];
                cout << "test\t" << str << endl;
                std::string str2(str);
                M[i][j] = stringtoint(str2);
                t--;
        }
    }
}


extern "C"
JNIEXPORT jstring JNICALL
Java_cn_edu_wust_scenicspotsystem_MainActivity_TravelAll(JNIEnv *env, jobject instance, jstring A_,
                                                         jint e, jint n, jint st) {
    if (st >= e || st < 0)
        return env->NewStringUTF("请输入合法的数据");

    const char *A = env->GetStringUTFChars(A_, 0);
    int M[MAXN][MAXN] = {0};
    // TODO
    Init(A, M);
    AdjGraph Adj;
    Adj.CreateAdj(M, n, e);
    std::string ans = Adj.TravelAll(st);
    char res[1000] = {0};
    for (int i = 0; i < ans.size(); i++)
        res[i] = ans[i];

    env->ReleaseStringUTFChars(A_, A);

    return env->NewStringUTF(res);
}


extern "C"
JNIEXPORT jstring JNICALL
Java_cn_edu_wust_scenicspotsystem_MainActivity_MSTree(JNIEnv *env, jobject instance, jstring A_,
                                                      jint e, jint n) {
    const char *A = env->GetStringUTFChars(A_, 0);

    int M[MAXN][MAXN] = {0};
    // TODO
    Init(A,M);
    AdjGraph Adj;
    Adj.CreateAdj(M,n,e);
    std::string ans = Adj.MSTree();
    char res[1000] = {0};
    for(int i = 0;i < ans.size();i++)
        res[i] = ans[i];
    // TODO

    env->ReleaseStringUTFChars(A_, A);

    return env->NewStringUTF(res);
}

extern "C"
JNIEXPORT jstring JNICALL
Java_cn_edu_wust_scenicspotsystem_MainActivity_STpah(JNIEnv *env, jobject instance, jstring A_,
                                                     jint e, jint n, jint st, jint en) {
    const char *A = env->GetStringUTFChars(A_, 0);

    int M[MAXN][MAXN] = {0};
    // TODO
    Init(A,M);
    AdjGraph Adj;
    Adj.CreateAdj(M,n,e);
    std::string ans = Adj.Floyd(st,en);

    char res[1000] = {0};
    for(int i = 0;i < ans.size();i++)
        res[i] = ans[i];
    // TODO

    env->ReleaseStringUTFChars(A_, A);

    return env->NewStringUTF(res);
}