#ifndef SCENICSPOTSYSTEM_STRING_EXT_H
#define SCENICSPOTSYSTEM_STRING_EXT_H
#include <string>
#include <queue>
#include <stack>
#include <cmath>
inline std::string operator + (const std::string &t, char x) {
    std::string ans = t;
    ans.insert(t.end(), x);
    return ans;
}
inline std::string operator + (const std::string &t, int x) {
    std::string ans = t;
    if (x < 0)
        ans = ans + "-";
    x = abs(x);
    std::stack<char> q;
    do {
        q.push(x % 10 + '0');
        x = x / 10;
    } while (x);

    while (!q.empty()) {
        ans.insert(ans.end(), q.top());
        q.pop();
    }

    return ans;
}

int stringtoint(const std::string &t) {
	int ans = 0;
	std::queue<int> q;
	for (int i = 0; i < t.size(); i++)
		q.push(t[i] - '0');
	while (!q.empty()) {
		ans = ans * 10 + q.front();
		q.pop();
	}

	return ans;
}
#endif //SCENICSPOTSYSTEM_STRING_EXT_H
