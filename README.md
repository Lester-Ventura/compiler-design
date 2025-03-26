![alt text](.github/image.png)

# LoLang

To setup the project
 - clone this repository using `git clone git@github.com:Lester-Ventura/compiler-design.git`
 - open your editor of choice and click the run button
   - if you're manually compiling, you can run this command inside the source folder to compile and execute the example file: `javac *.java && java Main ../Example.lol`

To run and generate graphs inside WSL:
 - install graphviz using `sudo apt install graphviz`
 - `javac *.java && java Main --no-interactive && dot output.dot -T png -o graph.png && wslview graph.png`