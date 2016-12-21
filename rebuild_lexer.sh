read -e -p "Do you want to rebuild tokens.flex? (Y/n) " choice

case $choice in
    n | N | no | No | NO ) exit 1;;
    *) buck run third-party:jflex-bin -- java/com/yuck/grammar/tokens.flex;;
esac
