import os
import tarfile
import io
import joblib
from sklearn.linear_model import LogisticRegression
import boto3

# Configuración
region = os.environ.get("AWS_REGION", "us-east-1")
bucket_name = os.environ.get("SAGEMAKER_S3_BUCKET", "finguard-sagemaker-artifacts")
model_key = "models/fraud-detection-v1/model.tar.gz"

# Crear modelo dummy
X = [[0], [1], [0], [1]]
y = [0, 1, 0, 1]
model = LogisticRegression()
model.fit(X, y)

# Serializar modelo a bytes
buffer = io.BytesIO()
joblib.dump(model, buffer)
buffer.seek(0)

# Crear archivo tar.gz en memoria
tar_buffer = io.BytesIO()
with tarfile.open(fileobj=tar_buffer, mode="w:gz") as tar:
    tarinfo = tarfile.TarInfo(name="model.joblib")
    tarinfo.size = len(buffer.getvalue())
    tar.addfile(tarinfo, io.BytesIO(buffer.getvalue()))
tar_buffer.seek(0)

# Subir a S3
s3 = boto3.client("s3", region_name=region)
s3.put_object(Bucket=bucket_name, Key=model_key, Body=tar_buffer.getvalue())
print(f"Modelo subido a s3://{bucket_name}/{model_key}")
