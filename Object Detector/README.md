# Object Detector for Android Client

안드로이드 스튜디오로 개발된 안드로이드 클라이언트 상에서 동작하는
tflite 모델을 만들기 위한 Model Maker입니다.

학습 데이터로는 OpenImage V4를 사용하며, 
모델을 만들기 위해 tflite_model_maker api를 사용합니다.

### 1. Environment Setting
- numpy
- tensorflow
- tflite_model_maker

### 2. Download OIDv4_Toolkit
```{.python}
!pip3 install -r requirements.txt

! python3 main.py downloader 
--classes Tablet_computer Pen Pencil_case (다운받고자 하는 class)
--noLabels (label의 다운로드 여부)
--type_csv train (train, validation, test 중 type 선택)
```

### 3. Train model
```{.python}
# Load Model mobilenet_SSD_v2
model = image_classifier.create(train_data, model_spec=model_spec.get('mobilenet_v2'))
```
- train data : 0.9
- validation data : 0.05
- test data : 0.05

학습 시키려는 모델 : Mobilenet_ssd_V2

### 4. Export model
