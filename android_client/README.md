#### android_client
android client/ android object detection
![image](https://user-images.githubusercontent.com/16810004/127075248-a3362d51-40b2-4331-8ed3-409bd73f343e.png)
![image](https://user-images.githubusercontent.com/16810004/127075276-dfcfe8f2-8f02-435f-a433-2522e57dbfbb.png)

#### 안드로이드 클라이언트 Class 설명

## AndroidManifest.xml
카메라, 인터넷, 네트워크 관련 권한이 필요합니다.
앱이 실행되는 순간에는 LoginActivity에서 시작하고, Main Activity의 경우에는 사용자의 카메라를 회전하지 않도록 고정하였습니다.

## UserInfo.java
사용자 정보를 다루기 위한 class입니다. 안드로이드에서 앱차원의 static variable을 사용하는 것은 권장되지 않기 때문에, Serializable을 implement하여, intent 간에 주고받을 수 있게 하였습니다. 간단한 getter, setter가 있고, 서버와의 통신에서 사용되는 parameter를 hashmap의 형태로 돌려주는 정도의 method가 존재합니다.

## ServerRequest.java
서버와의 통신을 위한 클래스입니다.
type에 따라서 Identification, endpoint, streaming start, streaming termination 등에 맞게 parameter를 설정하고, Http 통신을 수행한 후, response를 돌려주게 됩니다. java의 HttpURLConnection를 사용하고, request method, request property 등이 정해져 있습니다.

## LoginActivity.java
btn, btn1, btn2가 있는데,
btn0, btn1의 경우에는 각각 로그인 버튼 및 시험 참여 버튼에 해당합니다.

- onClick

btn0, btn1의 경우에는 각각 네트워크와 관련한 처리를 진행하므로, thread로 구현하였습니다.
login을 하게 되면 "calculus1.midterm_20200101" 의 형태의 response가 약속되었었기 때문에, ^\w*_\w*_\d{8}_\d{4}_\d{4}$ 의 정규표현식을 이용하였습니다. 
endpoint를 request하는 경우에는 rtmp://xxx.xxx.xxx.xxx... 의 형태가 가정되었기 때문에, 마찬가지로 정규표현식을 이용하여 확인합니다.
Endpoint를 request하는 경우에 {"0":"rtmp://3.35.108.14/channel2/092cd759-822c-46fe-83ba-da0d4246374f"} 다음과 같이 json 형식으로 변경되어 ServerRequest의 RESPONSE MESSAGE setting 부분에 그에 맞게 변경되어 있습니다.

# (중요) 
현재는 서버에 문제가 있어 강제로 절대 주소를 rtmp://xxx.xxx.xxx.xxx... 의 형태로 받아오도록 LoginActicity에서 설정해놓았습니다. 서버 상 rtmp url 반환 문제가 해결된다면 {"0":"rtmp://3.35.108.14/channel2/092cd759-822c-46fe-83ba-da0d4246374f"}의 형태로 받아올 수 있도록 REQUEST_RESPONSE를 변경하셔야 합니다. 

- onCreate

권한 요청을 위해서 TedPermission builder가 존재합니다.
사용자가 마지막으로 login창에 입력했던 이름, 학번을 불러오기 위해서 SharedPreferences를 이용합니다.

- GetMacAddress

모바일은 0, PC는 1의 MAC address를 사용하고 있으며, 이는 RTMP URL을 request하는 부분의 json에서도 알 수 있습니다.

- ShowToastMessage

non ui thread에서 Toast message가 수행되는 경우가 많아 따로 분리해두었습니다.

## MainActivity

rtmp-rtsp stream 관련 라이브러리를 사용합니다. (https://github.com/pedroSG94/rtmp-rtsp-stream-client-java)
OpenGlView, RtmpCamera2 가 주된 역할을 하는데, 둘 다 object 수준에서 extend한 거 같아서... 따로 건드리진 않았습니다.
TimerTask가 두 개가 존재하는데, 하나는 제가 디버깅용으로 사용했던 거고, 하나는 지속시간을 표시하기 위하여 사용되었습니다.
RtmpCamera2의 경우에는, SurfaceView (혹은 TextureView나 OpenGlView 등) 과 연결해주기만 하면 되고, 그 이외는 대부분 해당 class의 method를 이용하면 됩니다. method의 이름을 보면 직관적으로 이해할 수 있을 것이라 생각합니다.

- StreamManager

스트리밍의 시작, 끝을 관리하고, 화면을 변경하기 위하여 사용하였습니다.
RtmpCamer2의 경우 prepareVideo, prepareAudio 라는 method를 통해서 설정을 하고, 만약 실패하면 false를 돌려줍니다.
Streaming의 시작에서 서버와의 통신을 하기 때문에, 마찬가지로 서버와의 통신이 포함됩니다.

- onClick

카메라 변경의 경우, 촬영중이 아닐 때만 변경할 수 있도록 하면 되며,
streaming을 멈추는 경우는, 사용자가 명시적으로 스트리밍을 중지하는 순간에만 서버에 termination request를 하는 것으로 이야기가 되어 있고, 그에 맞게 구현되어 있습니다.
그 이외에 앱의 lifecycle이 변하는 등의 경우에는 StreamManager를 통해서 streaming을 중지하기는 하나, Termination을 보내지는 않습니다.

- useDefaultObjectDetector()

Default tflite 파일을 사용할 수 있는 함수이나, 저희가 train 한 tflite 모델을 사용할 것이기에 사용하지 않을 함수입니다.

- useCustomObjectDetector()

Custom tflite 파일을 사용할 수 있는 함수입니다.
https://developers.google.com/ml-kit/vision/object-detection/custom-models/android 사용 방법은 해당 링크를 참고하시면 자세히 알 수 있습니다.

- imageFromBuffer

ImageReader을 통해 image를 설정할 수 있는 함수입니다.

- getByteBuffer

Image 설정 시에 bytebuffer의 형태로 input을 얻어오고자할 때 필요한 함수입니다.

- getBitmap

Image 설정 시에 bitmap의 형태로 input을 얻어오고자할 때 필요한 함수입니다.
