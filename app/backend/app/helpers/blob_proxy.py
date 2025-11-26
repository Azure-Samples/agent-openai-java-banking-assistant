"""Simple proxy for Azure Blob Storage operations.

Provides small, tested-friendly helpers to read and write blobs as bytes.
Configuration is externalized in ``app.config.python`` which builds the
container client using the application's credential strategy.
"""
from typing import Optional
from azure.storage.blob import BlobClient
from azure.storage.blob import BlobServiceClient
from app.config.settings import settings
from app.config.azure_credential import get_azure_credential


class BlobStorageProxy:
	"""Proxy for common blob read/write operations.

	Usage:
		proxy = BlobStorageProxy(container_name="my-container")
		data = proxy.get_file_as_bytes("path/to/blob")
		proxy.store_file(b"bytes...", "path/to/blob")
	"""

	def __init__(self, container_name: str, client: BlobServiceClient) -> None:
		self._container_client = client.get_container_client(container_name)

	def get_file_as_bytes(self, file_name: str) -> bytes:
		"""Download a blob and return its content as bytes.

		Raises the underlying SDK exceptions on failure so callers can
		implement retries or translate errors to domain errors.
		"""
		blob_client: BlobClient = self._container_client.get_blob_client(file_name)
		downloader = blob_client.download_blob()
		return downloader.readall()

	def store_file(self, data: bytes, blob_name: str, overwrite: bool = True) -> None:
		"""Upload bytes to a blob.

		By default the upload will overwrite existing blobs. Set
		``overwrite=False`` to preserve existing blobs (the SDK will raise).
		"""
		blob_client: BlobClient = self._container_client.get_blob_client(blob_name)
		blob_client.upload_blob(data, overwrite=overwrite)
