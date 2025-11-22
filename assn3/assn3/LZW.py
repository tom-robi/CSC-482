# lzw_benchmark.py
import argparse
import random
import time
from typing import List, Dict

def read_words_from_file(path: str) -> List[str]:
    try:
        with open(path, "r", encoding="utf-8", errors="ignore") as f:
            # whitespace-delimited tokenization
            return f.read().split()
    except OSError as e:
        raise SystemExit(f"Error: could not open file: {path}\n{e}")

def join_sampled_words(words: List[str], n_words: int, seed: int) -> str:
    if n_words <= 0:
        raise SystemExit("Error: n_words must be positive.")
    use = min(n_words, len(words))
    idx = list(range(len(words)))
    rng = random.Random(seed)
    rng.shuffle(idx)
    sample = [words[i] for i in idx[:use]]
    return " ".join(sample)

def encoding(s: str) -> List[int]:
    """LZW encoding (string -> list of integer codes)."""
    if not s:
        return []
    # initialize dictionary with single-byte strings
    table: Dict[str, int] = {chr(i): i for i in range(256)}

    p = s[0]
    code = 256
    out: List[int] = []

    # Note: matches your C++ control flow (p + next char probe)
    for i in range(len(s)):
        c = s[i + 1] if i + 1 < len(s) else ""
        pc = p + c
        if pc in table:
            p = pc
        else:
            out.append(table[p])
            table[pc] = code
            code += 1
            p = c

    # flush last token
    out.append(table[p])
    return out

def decoding(op: List[int]) -> str:
    """LZW decoding (list of integer codes -> string)."""
    if not op:
        return ""
    table: Dict[int, str] = {i: chr(i) for i in range(256)}

    old = op[0]
    s = table[old]
    c = s[0]
    count = 256
    out = [s]

    for i in range(len(op) - 1):
        n = op[i + 1]
        if n not in table:
            s = table[old] + c
        else:
            s = table[n]
        out.append(s)
        c = s[0]
        table[count] = table[old] + c
        count += 1
        old = n

    return "".join(out)

def main():
    parser = argparse.ArgumentParser(
        description="Read words from file, sample N at random, LZW-compress, and time it."
    )
    parser.add_argument("path", help="Path to input text file")
    parser.add_argument("n_words", type=int, help="Number of words to sample")
    parser.add_argument(
        "seed",
        nargs="?",
        type=int,
        help="Optional RNG seed (int). If omitted, current time (ns) is used.",
    )
    args = parser.parse_args()

    # If no seed provided, use current time in ns (like C++ high-res clock)
    seed = args.seed if args.seed is not None else time.time_ns()

    words = read_words_from_file(args.path)
    if not words:
        raise SystemExit("Error: file contains no words.")

    # Build input text by sampling words without replacement
    input_text = join_sampled_words(words, args.n_words, seed)

    # Time compression
    t0 = time.perf_counter_ns()
    codes = encoding(input_text)
    t1 = time.perf_counter_ns()
    micros = (t1 - t0) // 1_000  # microseconds

    # Report stats (mirroring C++ style)
    print(f"Selected words: {min(args.n_words, len(words))}")
    print(f"Input bytes:    {len(input_text)}")
    print(f"Output codes:   {len(codes)} (LZW variable-width codes)")
    print(f"Compress time:  {micros} microseconds")
    print(f"Seed used:      {seed}")

    # # Write codes (space-separated) for inspection (not a compact binary format)
    # with open("compressed_codes.txt", "w", encoding="utf-8") as fout:
    #     fout.write(" ".join(str(x) for x in codes))
    # print("Wrote codes to compressed_codes.txt")


if __name__ == "__main__":
    main()

