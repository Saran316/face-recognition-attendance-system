
from fastapi import FastAPI, UploadFile, File, HTTPException
import cv2
import numpy as np
from deepface import DeepFace
from scipy.spatial.distance import cosine
import json
from fastapi import FastAPI, UploadFile, File, Form, HTTPException
app = FastAPI(title="Face Attendance Service")

# Load OpenCV's pre-trained face detector
face_detector = cv2.CascadeClassifier(
    cv2.data.haarcascades
    + "haarcascade_frontalface_default.xml"
)

MAX_IMAGE_SIZE = 5 * 1024 * 1024  # 5 MB


async def read_valid_image(upload: UploadFile):
    if not upload.content_type or not upload.content_type.startswith("image/"):
        raise HTTPException(
            status_code=400,
            detail="Please upload an image file."
        )

    image_bytes = await upload.read(MAX_IMAGE_SIZE + 1)

    if not image_bytes:
        raise HTTPException(
            status_code=400,
            detail="The uploaded image is empty."
        )

    if len(image_bytes) > MAX_IMAGE_SIZE:
        raise HTTPException(
            status_code=413,
            detail="Image size must not exceed 5 MB."
        )

    image_array = np.frombuffer(image_bytes, dtype=np.uint8)
    image = cv2.imdecode(image_array, cv2.IMREAD_COLOR)

    if image is None:
        raise HTTPException(
            status_code=400,
            detail="Unable to read the uploaded image."
        )

    return image


@app.get("/")
def home():
    return {
        "message": "Face Attendance Service is running"
    }

@app.post("/detect-face")
async def detect_face(file: UploadFile = File(...)):
    if not file.content_type or not file.content_type.startswith("image/"):
        raise HTTPException(
            status_code=400,
            detail="Please upload an image file."
        )

    image_bytes = await file.read()

    if not image_bytes:
        raise HTTPException(
            status_code=400,
            detail="The uploaded image is empty."
        )

    image_array = np.frombuffer(image_bytes, np.uint8)
    image = cv2.imdecode(image_array, cv2.IMREAD_COLOR)

    if image is None:
        raise HTTPException(
            status_code=400,
            detail="Unable to read the uploaded image."
        )

    gray = cv2.cvtColor(image, cv2.COLOR_BGR2GRAY)

    faces = face_detector.detectMultiScale(
        gray,
        scaleFactor=1.1,
        minNeighbors=5,
        minSize=(30, 30)
    )

    return {
        "face_detected": len(faces) > 0,
        "face_count": len(faces),
        "message": (
            "Face detected successfully"
            if len(faces) > 0
            else "No face detected"
        ),
        "faces": [
            {
                "x": int(x),
                "y": int(y),
                "width": int(w),
                "height": int(h)
            }
            for x, y, w, h in faces
        ]
    }
@app.post("/create-embedding")
async def create_embedding(file: UploadFile = File(...)):
    if not file.content_type or not file.content_type.startswith("image/"):
        raise HTTPException(
            status_code=400,
            detail="Please upload an image file."
        )

    image_bytes = await file.read()

    if not image_bytes:
        raise HTTPException(
            status_code=400,
            detail="The uploaded image is empty."
        )

    image_array = np.frombuffer(image_bytes, np.uint8)
    image = cv2.imdecode(image_array, cv2.IMREAD_COLOR)

    if image is None:
        raise HTTPException(
            status_code=400,
            detail="Unable to read the uploaded image."
        )

    try:
        representations = DeepFace.represent(
            img_path=image,
            model_name="Facenet512",
            detector_backend="opencv",
            enforce_detection=True
        )

        if len(representations) != 1:
            raise HTTPException(
                status_code=400,
                detail="Please upload an image containing exactly one face."
            )

        embedding = representations[0]["embedding"]

        return {
            "success": True,
            "model": "Facenet512",
            "embedding_dimensions": len(embedding),
            "message": "Face embedding created successfully",
            "embedding": embedding
        }

    except HTTPException:
        raise
    except Exception:
        raise HTTPException(
            status_code=400,
            detail="Could not create a face embedding. Use a clear, front-facing photo."
        )

@app.post("/compare-faces")
async def compare_faces(
    enrolled_image: UploadFile = File(...),
    new_image: UploadFile = File(...)
):
    async def read_image(upload: UploadFile):
        if not upload.content_type or not upload.content_type.startswith("image/"):
            raise HTTPException(
                status_code=400,
                detail="Please upload valid image files."
            )

        image_bytes = await upload.read()
        image_array = np.frombuffer(image_bytes, np.uint8)
        image = cv2.imdecode(image_array, cv2.IMREAD_COLOR)

        if image is None:
            raise HTTPException(
                status_code=400,
                detail="Unable to read an uploaded image."
            )

        return image

    first_image = await read_image(enrolled_image)
    second_image = await read_image(new_image)

    try:
        result = DeepFace.verify(
            img1_path=first_image,
            img2_path=second_image,
            model_name="Facenet512",
            detector_backend="opencv",
            enforce_detection=True
        )

        return {
            "verified": bool(result["verified"]),
            "distance": float(result["distance"]),
            "threshold": float(result["threshold"]),
            "model": "Facenet512"
        }

    except Exception:
        raise HTTPException(
            status_code=400,
            detail="Could not compare faces. Use clear, front-facing photos."
        )

from scipy.spatial.distance import cosine


@app.post("/compare-embeddings")
async def compare_embeddings(
    enrolled_embedding: str = Form(...),
    new_image: UploadFile = File(...)
):
    if not new_image.content_type or not new_image.content_type.startswith("image/"):
        raise HTTPException(
            status_code=400,
            detail="Please upload a valid image file."
        )

    try:
        stored_embedding = json.loads(enrolled_embedding)
        print("Parsed type:", type(stored_embedding).__name__)
        print("Embedding length:", len(stored_embedding) if isinstance(stored_embedding, list) else "Not a list")

        if (
            not isinstance(stored_embedding, list)
            or len(stored_embedding) != 512
            or not all(
                isinstance(value, (int, float))
                and not isinstance(value, bool)
                and np.isfinite(value)
                for value in stored_embedding
            )
        ):
            raise ValueError("Invalid embedding format.")

    except (json.JSONDecodeError, ValueError, TypeError) as exc:
        print("Embedding validation error:", repr(exc))
        raise HTTPException(
            status_code=400,
            detail="The enrolled face embedding is invalid."
        )

    image = await read_valid_image(new_image)
    if image is None:
        raise HTTPException(
            status_code=400,
            detail="Unable to read the uploaded image."
        )

    try:
        representations = DeepFace.represent(
            img_path=image,
            model_name="Facenet512",
            detector_backend="opencv",
            enforce_detection=True
        )

        if len(representations) != 1:
            raise HTTPException(
                status_code=400,
                detail="Please upload an image containing exactly one face."
            )

        new_embedding = representations[0]["embedding"]

        distance = float(
            cosine(stored_embedding, new_embedding)
        )

        if not np.isfinite(distance):
            raise ValueError("Invalid comparison result.")

        threshold = 0.30

        return {
            "verified": distance <= threshold,
            "distance": distance,
            "threshold": threshold,
            "model": "Facenet512"
        }

    except HTTPException:
        raise

    except Exception as exc:
        print("Face comparison error:", repr(exc))
        raise HTTPException(
            status_code=400,
            detail="Could not compare faces. Check the Python terminal for details."
        )