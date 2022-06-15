#don't worry about this file
#!/bin/bash
cd src
find . -name '*.class' -exec rm -rf {} \; 

cd ..
git add .
git commit -m "update"
git push