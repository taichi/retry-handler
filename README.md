# Thread.sleep とかねぇと思うな。

後、Runnableって自前インターフェースはいくら何でも酷いと思う。  
JDKに入ってない別な名前にするべき。  
加えて言うなら、Gradle可愛いよ、Gradle。

Futureの扱いがテキトー過ぎて、ちゃんと途中キャンセルできねぇので、  
sleep使う方が簡単な分、イマイチな事になってしもうた…。

## Requirements
* java7 tested by 1.7.0_02
* gradle 1.0-milestone-7 (for development)
* eclipse 3.7.1 (for development)

## License
Apache License, Version 2.0
