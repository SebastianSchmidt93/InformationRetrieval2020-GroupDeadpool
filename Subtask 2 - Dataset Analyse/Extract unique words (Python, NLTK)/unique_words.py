import nltk
import ijson
import re


url_regex = r"http[s]?://(?:[a-zA-Z]|[0-9]|[$-_@.&+]|[!*\(\),]|(?:%[0-9a-fA-F][0-9a-fA-F]))+"


def extract_sets(string):
    new_string = string.lower()

    url_set = set(re.findall(url_regex, new_string))
    # "..." will be removed in later step and does not mess up the tokenizer (like "" might)
    new_string = re.sub(url_regex, "...", new_string)

    words_set = set(nltk.word_tokenize(new_string))

    return {"urls": url_set, "words": words_set}


def remove_stopwords(words):
    new_words = []

    for word in words:
        if word not in nltk.corpus.stopwords.words('english'):
            new_words.append(word)
    return new_words


def filter_words(words):
    out_set = set()

    for word in words:
        # if "word" contains at least a digit or character, keep it
        if re.search(r"\w", word) is not None:
            # if word is actually two words separated with a dot, make it two words
            if re.search(r"^(\w+)\.(\w+)$", word) is not None:
                for new_word in word.split("."):
                    out_set.add(new_word)
            else:
                out_set.add(word)

    return out_set


def write_words_to_file(words, file_name, directory_path):
    out_string = ""

    for word in words:
        out_string += word + " "

    with open(directory_path + "/" + file_name, "x") as file:
        # Remove space at end and write to file
        file.write(out_string[:-1])

    print(f">> File {file_name} created")


def extract_unique_words(directory_path, file_names,
                         words_result_file_name="words_set.txt", urls_result_file_name="urls_set.txt",
                         no_stopwords_result_file_name="no_stopwords_set.txt"):
    words_set = set()
    url_set = set()

    for file_name in file_names:
        print(f">> Processing {file_name}")
        file_path = directory_path + "/" + file_name

        with open(file_path, "r") as file:
            arguments_json = ijson.items(file, "arguments.item")

            counter = 1

            for argument in arguments_json:
                source_sets = extract_sets(argument["conclusion"] + " " + argument["premises"][0]["text"])

                words_set = words_set.union(source_sets["words"])
                url_set = url_set.union(source_sets["urls"])

                print(f"{counter} arguments processed")
                counter += 1

    words_set = filter_words(words_set)
    no_stopwords_set = remove_stopwords(words_set)

    write_words_to_file(words_set, words_result_file_name, directory_path)
    write_words_to_file(url_set, urls_result_file_name, directory_path)
    write_words_to_file(no_stopwords_set, no_stopwords_result_file_name, directory_path)

    return no_stopwords_set


if __name__ == "__main__":
    directory_path = ""
    file_names = ["parliamentary.json", "idebate.json", "debatewise.json", "debatepedia.json", "debateorg.json"]

    no_stopwords = extract_unique_words(directory_path, file_names, words_result_file_name="words.txt",
                                        urls_result_file_name="urls.txt",
                                        no_stopwords_result_file_name="no_stopwords.txt")

    print(f"RESULT: After removing stopwords there are {len(no_stopwords)} words left.")
