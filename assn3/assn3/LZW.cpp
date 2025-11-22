#include <bits/stdc++.h>
using namespace std;
// --- added helpers ---
static vector<string> read_words_from_file(const string& path) {
    ifstream fin(path);
    if (!fin) throw runtime_error("Could not open input file: " + path);
    vector<string> words;
    words.reserve(1 << 20);
    string w;
    while (fin >> w) words.push_back(w);
    if (words.empty()) throw runtime_error("File contained no words.");
    return words;
}

static string join_sampled_words(const vector<string>& words,
                                long long n_words,
                                unsigned long long seed) {
    if (n_words <= 0) throw runtime_error("n_words must be positive.");
    long long use = min<long long>(n_words, (long long)words.size());
    vector<size_t> idx(words.size());
    iota(idx.begin(), idx.end(), 0);
    std::mt19937_64 rng(seed);
    shuffle(idx.begin(), idx.end(), rng);
    vector<string> sample;
    sample.reserve((size_t)use);
    for (long long i = 0; i < use; ++i) sample.push_back(words[idx[(size_t)i]]);
    // join with spaces
    size_t total = 0;
    for (auto& s : sample) total += s.size() + 1;
    string out; out.reserve(total ? total - 1 : 0);
    for (size_t i = 0; i < sample.size(); ++i) {
        if (i) out.push_back(' ');
        out += sample[i];
    }
    return out;
}

vector<int> encoding(string s1)
{
    cout << "Encoding\n";
    unordered_map<string, int> table;
    for (int i = 0; i <= 255; i++) {
        string ch = "";
        ch += char(i);
        table[ch] = i;
    }

    string p = "", c = "";
    p += s1[0];
    int code = 256;
    vector<int> output_code;
    // cout << "String\tOutput_Code\tAddition\n";
    for (int i = 0; i < s1.length(); i++) {
        if (i != s1.length() - 1)
            c += s1[i + 1];
        if (table.find(p + c) != table.end()) {
            p = p + c;
        }
        else {
            // cout << p << "\t" << table[p] << "\t\t" 
                 // << p + c << "\t" << code << endl;
            output_code.push_back(table[p]);
            table[p + c] = code;
            code++;
            p = c;
        }
        c = "";
    }
    // cout << p << "\t" << table[p] << endl;
    output_code.push_back(table[p]);
    return output_code;
}

void decoding(vector<int> op)
{
    cout << "\nDecoding\n";
    unordered_map<int, string> table;
    for (int i = 0; i <= 255; i++) {
        string ch = "";
        ch += char(i);
        table[i] = ch;
    }
    int old = op[0], n;
    string s = table[old];
    string c = "";
    c += s[0];
    cout << s;
    int count = 256;
    for (int i = 0; i < op.size() - 1; i++) {
        n = op[i + 1];
        if (table.find(n) == table.end()) {
            s = table[old];
            s = s + c;
        }
        else {
            s = table[n];
        }
        cout << s;
        c = "";
        c += s[0];
        table[count] = table[old] + c;
        count++;
        old = n;
    }
}
int main(int argc, char* argv[]) {
    ios::sync_with_stdio(false);
    cin.tie(nullptr);

    if (argc < 3) {
        cerr << "Usage: " << argv[0] << " <path/to/input.txt> <n_words> [seed]\n";
        return 1;
    }
    const string path = argv[1];
    long long n_words = atoll(argv[2]);
    unsigned long long seed;
    if (argc >= 4) {
        seed = strtoull(argv[3], nullptr, 10);
    } else {
        seed = (unsigned long long)
            chrono::high_resolution_clock::now().time_since_epoch().count();
    }

    try {
        // A) read file
        auto words = read_words_from_file(path);
        // B) choose n words at random (without replacement) and join
        string input = join_sampled_words(words, n_words, seed);
        // C) time compression
        auto t0 = chrono::high_resolution_clock::now();
        vector<int> output_code = encoding(input);
        auto t1 = chrono::high_resolution_clock::now();
        auto micros = chrono::duration_cast<chrono::microseconds>(t1 - t0).count();

        cout << "\n--- Stats ---\n";
        cout << "Selected words: " << min<long long>(n_words, (long long)words.size()) << "\n";
        cout << "Input bytes:    " << input.size() << "\n";
        cout << "Codes emitted:  " << output_code.size() << "\n";
        cout << "Compress time:  " << micros << " microseconds\n";

        // Optional: verify decoding correctness on demand (commented out)
        // decoding(output_code);  // beware: prints the reconstructed text to stdout

    } catch (const exception& e) {
        cerr << "Error: " << e.what() << "\n";
        return 1;
    }
    return 0;
}
