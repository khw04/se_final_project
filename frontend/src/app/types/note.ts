export type Note = {
  id: number
  title: string
  subjectId: number
  content: string
  attachmentIds: number[]
  preview: string
  updatedAt: string
  createdAt: string
}
