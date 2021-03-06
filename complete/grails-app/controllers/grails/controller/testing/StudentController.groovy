package grails.controller.testing

import static org.springframework.http.HttpStatus.*
import grails.transaction.Transactional

@Transactional(readOnly = true)
class StudentController {

    static allowedMethods = [save: "POST", update: "PUT", delete: "DELETE"]

    // tag::injectedStudentService[]
    StudentService studentService

    // end::injectedStudentService[]

    // tag::indexMethod[]
    def index(Integer max) {
        params.max = Math.min(max ?: 10, 100)
        respond Student.list(params), model:[studentCount: Student.count()]
    }
    // end::indexMethod[]

    def show(Student student) {
        respond student
    }

    def create() {
        respond new Student(params)
    }

    // tag::saveMethod[]
    @Transactional
    def save(Student student) {
        if (student == null) {
            transactionStatus.setRollbackOnly()
            notFound()
            return
        }

        if (student.hasErrors()) {
            transactionStatus.setRollbackOnly()
            respond student.errors, view:'create'
            return
        }

        student.save flush:true

        request.withFormat {
            form multipartForm {
                flash.message = message(code: 'default.created.message', args: [message(code: 'student.label', default: 'Student'), student.id])
                redirect student
            }
            '*' { respond student, [status: CREATED] }
        }
    }
    // end::saveMethod[]

    def edit(Student student) {
        respond student
    }

    // tag::updateMethod[]
    @Transactional
    def update(Student student) {

        def list = Student.list()
        if (student == null) {
            transactionStatus.setRollbackOnly()
            notFound()
            return
        }

        if (student.hasErrors()) {
            transactionStatus.setRollbackOnly()
            respond student.errors, view:'edit'
            return
        }

        student.save flush:true

        request.withFormat {
            form multipartForm {
                flash.message = message(code: 'default.updated.message', args: [message(code: 'student.label', default: 'Student'), student.id])
                redirect student
            }
            '*'{ respond student, [status: OK] }
        }
    }
    // end::updateMethod[]

    @Transactional
    def delete(Student student) {

        if (student == null) {
            transactionStatus.setRollbackOnly()
            notFound()
            return
        }

        student.delete flush:true

        request.withFormat {
            form multipartForm {
                flash.message = message(code: 'default.deleted.message', args: [message(code: 'student.label', default: 'Student'), student.id])
                redirect action:"index", method:"GET"
            }
            '*'{ render status: NO_CONTENT }
        }
    }

    protected void notFound() {
        request.withFormat {
            form multipartForm {
                flash.message = message(code: 'default.not.found.message', args: [message(code: 'student.label', default: 'Student'), params.id])
                redirect action: "index", method: "GET"
            }
            '*'{ render status: NOT_FOUND }
        }
    }

    // tag::calculateAvgGradeMethod[]
    def calculateAvgGrade() {
        BigDecimal avgGrade = studentService.calculateAvgGrade()
        render"Avg Grade is ${avgGrade}"
    }

    // end::calculateAvgGradeMethod[]
}
